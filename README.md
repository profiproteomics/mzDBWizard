# mzDBWizard
Application to manage raw files (conversion to mzdb, then conversion to mgf, upload to server)
  
### Release

#### 1.2.2 since 1.0.3

* Add read only option for configuration
* refactoring for mgf creation
  * mgfBoost usage (1.3.3)
* Add patch generated mzdb for DIA/PRM data
* Fix process pending option
* Add mzdbConverter support (1.2.5)
* Split: Update Master Scan Metadata to new scans indexing after split


#### 1.0.3 since 0.2.4

* Add converter options 
* bug fixes
  * file extension not case sensitive any more
  * try to upload mzdb file multiple times
  * start to upload files even if not all files are converted
  * upload configuration:  mount point not refresh when changing host
  * error in JMS communication with server
  
