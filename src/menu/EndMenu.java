package menu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class EndMenu extends JPanel
{
	JFrame frame;
	JLabel top;
	
	int width, height;
	
	Color blue = new Color(0, 162, 232);
	Color yellow = new Color(239, 228, 176);
	
	Font font = new Font("Arial", Font.BOLD, 40);
	
	boolean key_pressed = false;
	
	public EndMenu(JFrame frame, JLabel top, String final_time_str)
	{
		super();
			this.width  = frame.getWidth();
			this.height = (9 * frame.getHeight()) / 10;

		this.frame = frame;
		
		setPreferredSize(new Dimension(this.width, this.height));
		setLocation(0, 0);
		setBackground(blue);
		frame.add(this);
		
		top.setText("Good job!");
		this.top = top;
		
		if(!final_time_str.equals(""))
			initUserInpGUI(final_time_str);
		else
		{			
			this.top.setText("Vada a Bordo, Cazzo!");
			initEndPanel(null, "");
		}
	}
	
	// Name-input panel
	private void initUserInpGUI(String final_time_str)
	{
		setLayout(new GridLayout(3, 3));
		for(int i = 0; i < 4; i++)
		{
			JPanel filler_panel = new JPanel();
			filler_panel.setBackground(blue);
			add(filler_panel);
		}
		
		JPanel user_inp_panel = new JPanel();
		user_inp_panel.setLayout(new GridLayout(2, 1));
		add(user_inp_panel);
			JLabel time_display = new JLabel(final_time_str, JLabel.CENTER);
			time_display.setBackground(blue);
			time_display.setForeground(yellow);
			time_display.setFont(font);
			time_display.setOpaque(true);
			user_inp_panel.add(time_display);
			
			JTextField user_inp_field = new JTextField("Your Name:", JTextField.CENTER);
			user_inp_field.setBackground(blue);
			user_inp_field.setForeground(yellow);
			user_inp_field.setFont(font);
			user_inp_field.addKeyListener(new KeyAdapter()
			{
				public void keyPressed(KeyEvent press)
				{
					int user_inp = press.getKeyCode();

					// Commit name
					if(user_inp == KeyEvent.VK_ENTER)
					{
						String user_name = user_inp_field.getText();
						removeAll();
						
						// Leaderboard-disp. panel
						initEndPanel(user_name, final_time_str);
					}
					// Remove "Your Name:" when user pressed key
					else if(!key_pressed)
						user_inp_field.setText("");
				}
				public void keyReleased(KeyEvent release)
				{
					if(!key_pressed)
						key_pressed = true;
				}
			});
			user_inp_panel.add(user_inp_field);

		for(int i = 0; i < 4; i++)
		{
			JLabel filler = new JLabel();
			filler.setBackground(blue);
			add(filler);
		}
		revalidate();
	}
	
	// Panel with leaderboard and cont. options
	public void initEndPanel(String user_name, String final_time_str)
	{
		if(!final_time_str.equals(""))
		{			
			double tot_time = getTotTime(final_time_str);
			writeToLeaderboardMatrix(user_name, final_time_str, tot_time);
		}
		
		String[] leaderboard_matrix = getLeaderboardMatrix();
		leaderboard_matrix = sortLeaderboardMatrix(leaderboard_matrix);
		
		setPreferredSize(new Dimension(width, height));
		setLocation(0, 0);
		FlowLayout left = new FlowLayout(FlowLayout.LEFT, 0, 0);
		setLayout(left);
		
		// Top 10 list
		int width = 3 * (this.width / 5);
		JPanel leaderboard_container = new JPanel();
		leaderboard_container.setPreferredSize(new Dimension(width, this.height));
		leaderboard_container.setLocation(0, 0);
		leaderboard_container.setBackground(blue);
		add(leaderboard_container, LEFT_ALIGNMENT);
		leaderboard_container.setLayout(new GridLayout(10, 1));
		for(int i = 0; i < 10; i++)
		{
			if(i < leaderboard_matrix.length)
			{				
				String current_entry = "   ";
				current_entry += (i + 1);
				current_entry += ", ";
				current_entry += leaderboard_matrix[i];
				
				int i1 = -1, i2;
				for(i2 = 0; i2 < current_entry.length(); i2++)
				{
					if(current_entry.charAt(i2) == ';' && i1 == -1)
						i1 = i2;
					else if(current_entry.charAt(i2) == ';' && i1 != -1)
						break;
				}
				current_entry = current_entry.substring(0, i2);
				current_entry = current_entry.replace(";", ", ");
				
				JLabel l = new JLabel(current_entry, JLabel.LEFT);
				l.setForeground(yellow);
				l.setBackground(blue);
				l.setFont(font);
				l.setOpaque(true);
				leaderboard_container.add(l);
			}
			else
			{
				JPanel filler_panel = new JPanel();
				filler_panel.setForeground(yellow);
				filler_panel.setBackground(blue);
				filler_panel.setOpaque(true);
				leaderboard_container.add(filler_panel);
			}
		}
		
		width = this.width - ((21 * width) / 20);
		JPanel rest = new JPanel();
		rest.setPreferredSize(new Dimension(width, height));
		rest.setLocation(width, 0);
		rest.setBackground(blue);
		rest.setLayout(new GridLayout(5, 1));
		add(rest, RIGHT_ALIGNMENT);
			JPanel filler_top = new JPanel();
			filler_top.setBackground(blue);
			rest.add(filler_top);
			
			MenuButton menu_btn = new MenuButton("Main Menu", 4, 4, 4, 4);
			menu_btn.setFrame(frame);
			menu_btn.setTop(top);
			menu_btn.setPanel(this);
			rest.add(menu_btn);
			
			JPanel filler_mid = new JPanel();
			filler_mid.setBackground(blue);
			rest.add(filler_mid);
			
			MenuButton exit_btn = new MenuButton("Exit", 4, 4, 4, 4);
			exit_btn.setOpaque(true);
			rest.add(exit_btn);
			
			JPanel filler_bottom = new JPanel();
			filler_bottom.setBackground(blue);
			rest.add(filler_bottom);
		revalidate();
	}
	
	// Convert game-time to seconds
	private double getTotTime(String final_time_str)
	{
		int i1 = -1, i2 = -1, i3;
		double HH, MM, SS, sub_SS;
		for(i3 = 0; i3 < final_time_str.length(); i3++)
		{
			if(final_time_str.charAt(i3) == ':' && i2 == -1 && i1 == -1)
				i1 = i3;
			else if(final_time_str.charAt(i3) == ':' && i1 != -1)
				i2 = i3;
			else if(final_time_str.charAt(i3) == '.')
				break;
		}
		String HH_str = final_time_str.substring(0, i1);
			HH = Double.valueOf(HH_str);
		String MM_str = final_time_str.substring(i1 + 1, i2);
			MM = Double.valueOf(MM_str);
		String SS_str = final_time_str.substring(i2 + 1, i3);
			SS = Double.valueOf(SS_str);
		String sub_SS_str = final_time_str.substring(i3 + 1, final_time_str.length());
			sub_SS = Double.valueOf(sub_SS_str);
		
		return (3600 * HH) + (60 * MM) + SS + (sub_SS / 100);
	}
	
	// Add new entry to leaderboard file
	private void writeToLeaderboardMatrix(String user_name, String final_time_str, double tot_time)
	{
		StringBuffer new_entry = new StringBuffer();
		new_entry.append(user_name);
		new_entry.append(";");
		new_entry.append(final_time_str);
		new_entry.append(";");
		new_entry.append(tot_time);
		new_entry.append(";");
		
		BufferedWriter writer;
		File file = new File("scoreboard.txt");
		try
		{
			if(!file.exists())
				file.createNewFile();
			writer = new BufferedWriter(new FileWriter("scoreboard.txt", true));
			writer.append(new_entry.toString());
			writer.append('\n');

			writer.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	// Get content of leaderboard file
	private String[] getLeaderboardMatrix()
	{
		String[] leaderboard = null;
		
		File file = new File("scoreboard.txt");
		Scanner reader = null;
		try
		{
			reader = new Scanner(file);
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		
		int index = 0;
		while(reader.hasNextLine())
		{
			// Append array
			String[] temp_leaderboard = new String[index + 1];
			int i;
			for(i = 0; i < index; i++)
				temp_leaderboard[i] = leaderboard[i];
			temp_leaderboard[i] = "";
			leaderboard = temp_leaderboard;
			
			String current_line = reader.nextLine();
			leaderboard[i] = current_line;
			index++;
		}
		
		return leaderboard;
	}

	// Sort leaderboard with respect to time, shortest --> longest
	private String[] sortLeaderboardMatrix(String[] matrix)
	{
		int len = matrix.length;
		double[] tot_time_arr = new double[len];
		
		for(int i = 0; i < len; i++)
		{
			String current_str = matrix[i];
			int i1 = -1, i2 = -1, i3;
			for(i3 = 0; i3 < current_str.length(); i3++)
			{
				if(current_str.charAt(i3) == ';' && i2 != -1 && i1 != -1)
					break;
				else if(current_str.charAt(i3) == ';' && i1 != -1)
					i2 = i3;
				else if(current_str.charAt(i3) == ';' && i1 == -1)
					i1 = i3;
			}
			
			String tot_time_str = current_str.substring(i2 + 1, i3);
			double tot_time = 0;
			try
			{
				tot_time = Double.valueOf(tot_time_str);
			} catch (Exception e)
			{
				System.err.println("str --> double");
				e.printStackTrace();
				return null;
			}
			
			tot_time_arr[i] = tot_time;
		}
		
		String[] sorted_matrix = sort(matrix, tot_time_arr);
		return sorted_matrix;
	}

	// Actually sorting
	private String[] sort(String[] matrix, double[] tot_time_arr)
	{
		int placed_wrong = 0;
		for(int i = 1; i < matrix.length; i++)
		{
			if(tot_time_arr[i - 1] > tot_time_arr[i])
			{
				double temp = tot_time_arr[i];
				tot_time_arr[i] = tot_time_arr[i - 1];
				tot_time_arr[i - 1] = temp;
				
				String temp_str = matrix[i];
				matrix[i] = matrix[i - 1];
				matrix[i - 1] = temp_str;
				
				placed_wrong++;
			}
		}
		
		if(placed_wrong > 0)
			matrix = sort(matrix, tot_time_arr);
		return matrix;
	}
}