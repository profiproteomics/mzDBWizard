/*
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.profi.mzDBWizard.processing.info;

import java.util.ArrayList;

/**
 *
 * TaskInfo contains all the information (name, logs, error) of a Task
 *
 * @author JPM235353
 *
 */
public class TaskInfo  implements Comparable<TaskInfo> {


    public static final int CONVERTER_TASK = 0;
    public static final int UPLOAD_TASK = 1;
    public static final int DELETE_TASK = 2;
    public static final int MOUNTING_POINT_TASK = 3;
    public static final int GENERATE_TASK = 4;


    public final static int PUBLIC_STATE_WAITING = 0;
    public final static int PUBLIC_STATE_RUNNING = 1;
    public final static int PUBLIC_STATE_ABORTED = 2;
    public final static int PUBLIC_STATE_FINISHED = 3;
    public final static int PUBLIC_STATE_FAILED = 4;
    public final static String[] PUBLIC_STATE_VALUES = { "Waiting", "Running", "Aborted", "Finished", "Failed" };


    public final static int STATE_WAITING = 0;
    public final static int STATE_RUNNING = 1;
    public final static int STATE_FINISHED = 2;
    public final static int STATE_ABORTED = 3;

    public enum VisibilityEnum {
        VISIBLE,
        VISIBLE_IF_ERROR,
        HIDDEN
    };

    private int m_taskType;
    private String m_taskDescription = null;

    private String m_requestContent = null;
    private String m_requestURL = null;
    private VisibilityEnum m_visibility = VisibilityEnum.VISIBLE;

    private int m_state;
    private int m_updateState = -1;

    private long m_askTimestamp = -1;
    private long m_startTimestamp = -1;
    private long m_endTimestamp = -1;

    private float m_percentage = 0;


    private long m_duration = -1; // used for services where there is no start or end time

    private int m_id;
    private String m_jmsMessageID = null;

    private boolean m_askBeforeExitingApp;

    private boolean m_success;
    private TaskError m_taskError = null;

    private ArrayList<String> m_logs = new ArrayList<>();
    private int m_warningCount = 0;

    private TaskInfo m_srcTaskInfo = null;

    private static int INC_ID = 0;

    public TaskInfo(String taskDescription, int taskType, boolean askBeforeExitingApp) {
        this(taskDescription, taskType, askBeforeExitingApp,   VisibilityEnum.VISIBLE);
    }

    public TaskInfo(String taskDescription, int taskType, boolean askBeforeExitingApp, VisibilityEnum visibility) {

        m_taskDescription = taskDescription;
        m_taskType = taskType;
        m_askBeforeExitingApp = askBeforeExitingApp;
        m_visibility = visibility;

        m_state = STATE_WAITING;

        m_id = INC_ID++;

        m_success = true;
        m_taskError = null;

        m_askTimestamp = System.currentTimeMillis();
    }

    public TaskInfo(TaskInfo src) {
        copy(src, this);
    }

    public int getTaskType() {
        return m_taskType;
    }

    public void addLog(String log) {
        m_logs.add(log);
    }
    public void addWarning(String warning) {
        m_logs.add(warning);
        m_warningCount++;
    }


    public int getWarningCount() {
        return m_warningCount;
    }

    public ArrayList<String> getLogs() {
        return m_logs;
    }

    public void insertLogs(ArrayList<String> logs) {
        ArrayList<String> logsConcatenated = new ArrayList<>(logs);
        logsConcatenated.addAll(m_logs);
        m_logs = logsConcatenated;
    }

    public String getJmsMessageID() {
        return m_jmsMessageID;
    }

    public void setJmsMessageID(String jmsMsgId){
        this.m_jmsMessageID = jmsMsgId;
    }

    public void setRequestURL(String requestURL) {
        m_requestURL = requestURL;
    }

    public void setRunning(boolean saveTimestamp) {

        if ((m_state == STATE_RUNNING) || (m_updateState == STATE_RUNNING) || (m_updateState == STATE_FINISHED) || (m_updateState == STATE_ABORTED)) {
            // already running (or service finished, so afterwards little modification of database will not be taken in account)
            return;
        }

        m_updateState = STATE_RUNNING;
        if (saveTimestamp) {
            m_startTimestamp = System.currentTimeMillis();
        }
        TaskInfoManager.getTaskInfoManager().update(this);

    }

    public void setRequestContent(String m_requestContent) {
        this.m_requestContent = m_requestContent;
    }


    public void setAborted() {
        m_updateState = STATE_ABORTED;
        m_percentage = 0;
        TaskInfoManager.getTaskInfoManager().update(this);
    }

    public void setFinished(boolean success, TaskError taskError, boolean saveTimestamp) {

        if (m_updateState == STATE_ABORTED) {
            return;
        }

        if (m_updateState == STATE_FINISHED) {
            // already finished : so afterwards little modification of database will not be taken in account
            saveTimestamp = false;
        }
        m_updateState = STATE_FINISHED;
        if (saveTimestamp) {
            if (m_startTimestamp == -1) {
                // in fact nothing has been done : data was already loaded
                m_endTimestamp = -1;
            } else {
                m_endTimestamp = System.currentTimeMillis();
            }
        }
        m_percentage = 100;
        m_success = success;
        m_taskError = taskError;

        TaskInfoManager.getTaskInfoManager().update(this);
    }

    public void setDuration(long duration) {
        m_duration = duration;
        TaskInfoManager.getTaskInfoManager().update(this, false);
    }

    public void update() {
        if (m_updateState != -1) {
            m_state = m_updateState;
            m_updateState = -1;
        }
    }

    public boolean isWaiting() {
        return m_state == STATE_WAITING;
    }

    public boolean isRunning() {
        return m_state == STATE_RUNNING;
    }

    public boolean isFinished() {
        return m_state == STATE_FINISHED;
    }

    public boolean isAborted() {
        return m_state == STATE_ABORTED;
    }

    public boolean isSuccess() {
        return m_success;
    }

    public TaskError getTaskError() {

        return m_taskError;
    }

    public boolean hasTaskError() {
        return (m_taskError != null);
    }

    public int getId() {
        return m_id;
    }


    public int getPublicState() {
        if (isWaiting()) {
            return PUBLIC_STATE_WAITING;
        } else if (isRunning()) {
            return PUBLIC_STATE_RUNNING;
        } else if (isFinished()) {
            if (isSuccess()) {
                return PUBLIC_STATE_FINISHED;
            } else {
                return PUBLIC_STATE_FAILED;
            }
        } else if (isAborted()) {
            return PUBLIC_STATE_ABORTED;
        }
        return PUBLIC_STATE_WAITING; // should not happen
    }
    public String getPublicStateAsString() {
        return PUBLIC_STATE_VALUES[getPublicState()];
    }


    public boolean askBeforeExitingApplication() {
        return m_askBeforeExitingApp;
    }

    public String getTaskDescription() {
        return m_taskDescription;
    }

    public String getRequestContent() {
        return m_requestContent;
    }


    public long getAskTimestamp() {
        return m_askTimestamp;
    }
    public long getStartTimestamp() {
        return m_startTimestamp;
    }
    public long getEndTimestamp() {
        return m_endTimestamp;
    }

    public boolean isVisible() {
        return  (m_visibility == VisibilityEnum.VISIBLE) || ((m_visibility == VisibilityEnum.VISIBLE_IF_ERROR) && (m_taskError != null));
    }

    public long getDuration() {
        if (m_duration != -1) {
            return m_duration;
        }
        if ((m_endTimestamp!=-1) && (m_startTimestamp!=-1)) {
            return m_endTimestamp-m_startTimestamp;
        }
        return -1;
    }

    public long getDelay() {
        if ((m_startTimestamp!=-1) && (m_askTimestamp!=-1)) {
            return m_startTimestamp-m_askTimestamp;
        }
        return -1;
    }



    @Override
    public int compareTo(TaskInfo o) {

        // STATE_ABORTED and STATE_FINISHED are put at the same level for the sorting
        int state = m_state;
        if (state == STATE_ABORTED) {
            state = STATE_FINISHED;
        }

        int o_state = o.m_state;
        if (o_state == STATE_ABORTED) {
            o_state = STATE_FINISHED;
        }

        int cmp = state-o_state;
        if (cmp != 0) {
            return cmp;
        }
        return o.m_id-m_id;

    }

    public void copyData(TaskInfo dest) {
        copy(this, dest);
    }

    private static void copy(TaskInfo from, TaskInfo to) {
        to.m_taskDescription = from.m_taskDescription;
        to.m_taskType = from.m_taskType;
        to.m_askBeforeExitingApp = from.m_askBeforeExitingApp;
        to.m_visibility = from.m_visibility;
        to.m_state = from.m_state;
        to.m_id = from.m_id;
        to.m_success = from.m_success;
        to.m_taskError = from.m_taskError;
        to.m_askTimestamp = from.m_askTimestamp;
        to.m_startTimestamp = from.m_startTimestamp;
        to.m_endTimestamp = from.m_endTimestamp;
        to.m_duration = from.m_duration;
        to.m_percentage = from.m_percentage;
        to.m_requestContent = from.m_requestContent;
        to.m_jmsMessageID = from.m_jmsMessageID;

        to.m_srcTaskInfo = from;

        to.m_logs = new ArrayList<>(from.m_logs.size());
        for (String log : from.m_logs) {
            to.m_logs.add(log);
        }

    }

    public TaskInfo getSourceTaskInfo() {
        return m_srcTaskInfo;
    }

    @Override
    public String toString() {
        return "";
    }


}
