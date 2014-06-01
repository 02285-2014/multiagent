package custommas.app;

import java.io.IOException;

import eis.*;
import eis.exceptions.ManagementException;

public class App {

	public static void main(String[] args) {
		//  instantiate interpreter
		System.out.println("PHASE 1: INSTANTIATING INTERPRETER");
		AgentsInterpreter interpreter = null;
		if (args.length != 0){ 
			interpreter = new custommas.app.CustomAgentsInterpreter(args[0]);
		}else{
			interpreter = new custommas.app.CustomAgentsInterpreter();
		}
		System.out.println("interpreter loaded");

		// load the interface
		System.out.println("");
		System.out.println("PHASE 2: INSTANTIATING ENVIRONMENT");
		EnvironmentInterfaceStandard ei = null;
		
		try {
			ei = EILoader.fromClassName("massim.eismassim.EnvironmentInterface");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}

		System.out.println("environment-interface loaded");

		// start the interface
		try {
			ei.start();
		} catch (ManagementException e) {
			e.printStackTrace();
		}
		System.out.println("environment-interface started");

		System.out.println("");
		System.out.println("PHASE 3: CONNECTING INTERPRETER AND ENVIRONMENT");
		//  connect to environment
		interpreter.addEnvironment(ei);
		System.out.println("interpreter and environment connected");
				
		//  run stepwise
		System.out.println("");
		System.out.println("PHASE 4: RUNNING");
		int step = 1;
		boolean running = true;
		while ( running ) {
			System.out.println("STEP " + step);
			interpreter.step();
			step ++;
			System.out.println("");
			System.out.println("");
			System.out.println("");
		}
	}

}
