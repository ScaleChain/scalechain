config files in unittests/config this folder is used only for unittests

When you run unit tests only in scalechain-cli, you need to change the working directory for IntelliJ Run Configuration to the parent of this folder, otherwise test fails.
(The unit tests read configuration files in unittest/config and it assumes that the current working directory is scalechain, not scalechain-cli)
