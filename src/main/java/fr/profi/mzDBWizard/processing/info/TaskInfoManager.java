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

import fr.profi.mzDBWizard.gui.overview.AttributeEntry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

/**
 *
 * TaskInfoManager takes care of a list of TaskInfo and of the updates of this list.
 *
 * @author JPM235353
 *
 */
public class TaskInfoManager {

    private static final int MAX_SIZE = 5000;

    private TreeSet<TaskInfo> m_tasks;
    private ArrayList<TaskInfo> m_taskToBeUpdated;

    private static TaskInfoManager m_singleton = null;

    private int m_lastUpdate = -1;
    private int m_curUpdate = 0;

    private TaskInfoListener m_taskInfoListener;

    public static TaskInfoManager getTaskInfoManager() {
        if (m_singleton == null) {
            m_singleton = new TaskInfoManager();
        }
        return m_singleton;
    }

    private TaskInfoManager() {
        m_tasks = new  TreeSet<>();
        m_taskToBeUpdated = new ArrayList<>();
    }

    public void setListener(TaskInfoListener taskInfoListener) {
        m_taskInfoListener = taskInfoListener;
    }

    public synchronized void add(TaskInfo taskInfo) {
        m_tasks.add(taskInfo);
        if (m_tasks.size()>MAX_SIZE) {
            m_tasks.remove(m_tasks.last());
        }
        m_curUpdate++;

        if (m_taskInfoListener != null) {
            m_taskInfoListener.update();
        }
    }

    public synchronized void clear() {
        TreeSet<TaskInfo> tasks = new  TreeSet<>();
        for (TaskInfo task : m_tasks) {
            if (task.isFinished() && task.isSuccess()) {
                continue;
            }
            tasks.add(task);
        }
        m_tasks.clear();
        m_tasks = tasks;

        ArrayList<TaskInfo> taskToBeUpdated = new ArrayList<>();
        for (TaskInfo task : m_taskToBeUpdated) {
            if (task.isFinished() && task.isSuccess()) {
                continue;
            }
            taskToBeUpdated.add(task);
        }
        m_taskToBeUpdated.clear();
        m_taskToBeUpdated = taskToBeUpdated;

        m_curUpdate++;

        if (m_taskInfoListener != null) {
            m_taskInfoListener.update();
        }
    }

    public synchronized void cancel(TaskInfo taskInfo) {
        m_tasks.remove(taskInfo);
        m_curUpdate++;

        if (m_taskInfoListener != null) {
            m_taskInfoListener.update();
        }
    }

    public synchronized TaskInfo getTaskInfoWithJMSId(String jmsId){
        if(jmsId==null)
            return null;
        Iterator<TaskInfo> allTaskInfoIt = m_tasks.iterator();
        while(allTaskInfoIt.hasNext()){
            TaskInfo nextOne = allTaskInfoIt.next();
            if(jmsId.equals(nextOne.getJmsMessageID()) )
                return nextOne;
        }
        return null;
    }

    public synchronized void update(TaskInfo taskInfo) {
        m_taskToBeUpdated.add(taskInfo);
        m_curUpdate++;

        if (m_taskInfoListener != null) {
            m_taskInfoListener.update();
        }
    }

    public synchronized void update(TaskInfo taskInfo, boolean changeSorting) {
        if (changeSorting) {
            update(taskInfo);
        } else {
            m_curUpdate++;

            if (m_taskInfoListener != null) {
                m_taskInfoListener.update();
            }
        }
    }

    public synchronized boolean askBeforeExitingApp() {

        if (isUpdateNeeded()) {
            updateAll();
        }

        Iterator<TaskInfo> it = m_tasks.iterator();
        while (it.hasNext()) {
            TaskInfo infoCur = it.next();
            if ((infoCur.isWaiting() || infoCur.isRunning()) && infoCur.askBeforeExitingApplication()) {
                return true;
            }
        }

        return false;

    }

    private synchronized void updateAll() {

        int nb = m_taskToBeUpdated.size();
        for (int i=0;i<nb;i++) {
            TaskInfo info = m_taskToBeUpdated.get(i);
            m_tasks.remove(info);
            info.update();
            m_tasks.add(info);
        }
        m_taskToBeUpdated.clear();

    }

    public synchronized boolean isUpdateNeeded() {
        return (m_curUpdate != m_lastUpdate);
    }

    public synchronized boolean copyData(ArrayList<TaskInfo> destinationList, boolean copyHiddenTasks) {

        if ((m_curUpdate == m_lastUpdate) && (!destinationList.isEmpty())) {
            return false;
        }
        m_lastUpdate = m_curUpdate;

        updateAll();

        int sizeDestination = destinationList.size();
        int index = 0;
        Iterator<TaskInfo> it = m_tasks.iterator();
        while (it.hasNext()) {
            TaskInfo infoCur = it.next();

            // if needed, hide to user some minor task (if there is no error)
            if ((!copyHiddenTasks) && (!infoCur.isVisible())) {
                continue;
            }

            if (index<sizeDestination) {
                infoCur.copyData(destinationList.get(index));
            } else {
                destinationList.add(new TaskInfo(infoCur));
            }
            index++;
        }
        // can happen when table has been cleared
        if (destinationList.size() > index) {
            destinationList.subList(index, destinationList.size()).clear();
        }

        return true;
    }


    public ArrayList<AttributeEntry> getExecutionModelData() {


        int waiting = 0;
        int running = 0;
        int aborted = 0;
        int finished = 0;
        int failed = 0;
        Iterator<TaskInfo> it = m_tasks.iterator();
        while (it.hasNext()) {
            TaskInfo infoCur = it.next();

            // if needed, do not count some minor task (if there is no error)
            if (!infoCur.isVisible()) {
                continue;
            }

            switch (infoCur.getPublicState()) {
                case TaskInfo.PUBLIC_STATE_WAITING:
                    waiting++;
                    break;
                case TaskInfo.PUBLIC_STATE_RUNNING:
                    running++;
                    break;
                case TaskInfo.PUBLIC_STATE_ABORTED:
                    aborted++;
                    break;
                case TaskInfo.PUBLIC_STATE_FINISHED:
                    finished++;
                    break;
                case TaskInfo.PUBLIC_STATE_FAILED:
                    failed++;
                    break;

            }

        }

        ArrayList<AttributeEntry> modelData = new ArrayList<AttributeEntry>();
        modelData.add(new AttributeEntry("Total Tasks", String.valueOf(waiting+running+aborted+finished+failed)));
        modelData.add(new AttributeEntry("Active", String.valueOf(running)));
        modelData.add(new AttributeEntry("Finished", String.valueOf(finished)));
        modelData.add(new AttributeEntry("Waiting", String.valueOf(waiting)));
        modelData.add(new AttributeEntry("Failed", String.valueOf(failed)));
        return modelData;
    }


}
