package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MAIN
{
	public static void main(String[] args)
	{
		JFrame frame = new JFrame();
		frame.setUndecorated(true);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setSize(1920, 1080);
		frame.setLocationRelativeTo(null);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(true);
//		frame.setLayout(new GridLayout(1, 1));
		FlowLayout flow = new FlowLayout(FlowLayout.CENTER, 0, 0);
		frame.setLayout(flow);
//		flow.setHgap(-5);
		
		System.out.println(frame.getSize());
		JLabel top = new JLabel("00:00:00.00", JLabel.CENTER);
		top.setPreferredSize(new Dimension(frame.getWidth(), frame.getHeight() / 10));
		top.setLocation(0, 0);
		top.setFont(new Font("Arial", Font.BOLD, 50));
		top.setBackground(Color.RED);
		top.setForeground(Color.BLUE);
//		top.setBorder(BorderFactory.createMatteBorder(0, 0, 50, 0, Color.BLACK));
		top.setOpaque(true);
		frame.add(top);
	
		GAME_PANEL game_panel = new GAME_PANEL(frame, top.getHeight());
	}
}