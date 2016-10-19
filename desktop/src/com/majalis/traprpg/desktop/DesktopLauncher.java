package com.majalis.traprpg.desktop;

import java.lang.Thread.UncaughtExceptionHandler;

import org.lwjgl.Sys;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.majalis.traprpg.TrapRPG;
/*
 * Entry point of the package for desktop implementations - sets configuration elements and initializes the generic entry point.
 */
public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.addIcon("icon16.png", Files.FileType.Internal);
		config.addIcon("icon32.png", Files.FileType.Internal);
		config.addIcon("icon128.png", Files.FileType.Internal);
		config.title = "tRaPG";
		config.width = 2048;
		config.height = 1150;
		//config.fullscreen = true;
		//config.vSyncEnabled = true;
		config.allowSoftwareMode = true;
		
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
	         @Override
	         public void uncaughtException (Thread thread, final Throwable ex) {
	            System.err.println("Critical Failure" + ex.getLocalizedMessage());
	            Sys.alert("Critical Failure", "Sorry, fatal error - please let Majalis know!  Error: " + ex.getLocalizedMessage());
	         }
	      });
		
		new LwjglApplication(new TrapRPG(), config);
	}
}
