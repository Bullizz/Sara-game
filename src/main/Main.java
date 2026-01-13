package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;

import menu.StartMenu;

import handlers.AudioHandler;

public class Main
{
	public static void main(String[] args)
	{
		// Init. frame
		JFrame frame = new JFrame();
		frame.setUndecorated(true);
		frame.setTitle("Vada a Bordo, Cazzo!");
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setSize(1920, 1080);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		FlowLayout flow = new FlowLayout(FlowLayout.CENTER, 0, 0);
		frame.setLayout(flow);
		
		JLabel top = new JLabel("Vada a Bordo, Cazzo!", JLabel.CENTER);
		top.setPreferredSize(new Dimension(frame.getWidth(), frame.getHeight() / 10));
		top.setLocation(0, 0);
		top.setFont(new Font("Arial", Font.BOLD, 50));
		top.setBackground(new Color(0, 162, 232));
		top.setForeground(new Color(239, 228, 176));
		top.setOpaque(true);
		frame.add(top);

		AudioHandler game_audio = new AudioHandler("", -1);
		new StartMenu(frame, top, game_audio);
	}
}