# AWS EC2 Wake Launcher
Did you ever find yourself in this situation:
- You have a server that needs to run on AWS EC2.
- The server does not have that much traffic, but...
- ...running the server costs a heck of money.

Here is where this project comes into play. WakeLauncher is a tomcat webapp that can put your EC2 instance to sleep and wake it up once you need it again.

## But wait! You said its a web app, so I need a Tomcat server for it!
You are right, but: [Heroku](https://www.heroku.com/) offers free and simple hosting for web apps.

## How it works
1. Add the application client to your application [WIP]
3. Whenever you want to request something from your EC2 instance, you first call the application client. The application client then pings WakeLauncher. If your instance is already running, WakeLauncher will tell you and you may proceed with the actual request. If the instance is not running, WakeLauncher will boot it and tell you when it's ready. 
4. Whenever your server is idle, you shut it down however you want (e. g. using `shutdown /s /t 0` on windows or by using the AWS EC2 SDK).

## Some things to keep in mind
- WakeLauncher requires you to have an instance already configured. 
- Your server application must start automatically when the instance boots up, WakeLauncher cannot issue any shell commands on the instance.
- WakeLauncher is not intended and has not been tested with more complex setups (i. e. multiple instances and load balancing). If you wish to use it for that purpose and are willing to spend the time for testing and development, contributions are welcome!

## Download
I just started this project, give the repo a star and click the `Watch` button and you will be the first one to be notified about the launch.

## Usage
### Application client
#### Java
Add the following maven dependency [WIP]

You may now do the following:

```java
import com.github.vatbub.awsec2wakelauncher.applicationclient.Client;

public class Sample{
	public void sample(){
		Client client = new Client (new URL("localhost"));
		launchAndWaitForInstance(instanceId);
	}
}
```

#### JSON interface
When using any other programming language, you may interact with WakeLauncher using its JSON interface. [WIP]

## Building WakeLauncher
We use maven as our build tool. Therefore, run `mvn package` to build WakeLauncher.

**Please note:** We use [spulec/moto](https://github.com/spulec/moto) to mock the AWS EC2 api for tests. For that reason, you must also install Python. Windows users may need to run the first build with administrative privileges for `pip` to be able to install `moto`. 
