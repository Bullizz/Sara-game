package main;

import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;

public class MAIN
{
	public static void main(String[] args)
	{
		JFrame frame = new JFrame();
		frame.setUndecorated(false);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setSize(1920, 1080);
		frame.setLocationRelativeTo(null);
		frame.setResizable(true);
		frame.setLayout(new GridLayout(1, 1));
		
		System.out.println(frame.getSize());
		
		GAME_PANEL game_panel = new GAME_PANEL(frame);
	}
}