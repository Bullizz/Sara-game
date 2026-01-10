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
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import handlers.AudioHandler;

public class EndMenu extends JPanel
{
	JFrame frame;
	JLabel top;
	AudioHandler this_game_audio;
	
	int width, height;
	
	Color blue = new Color(0, 162, 232);
	Color yellow = new Color(239, 228, 176);
	
	Font font = new Font("Arial", Font.BOLD, 40);
	
	MenuButton menu_btn, clear_leaderboard_btn;
	
	// Different statuses this class can have
	String leaderboard_status;
	String leaderboard_default 	= "leaderboard_default",
		   leaderboard_cleared 	= "leaderboard_cleared",
		   string_user_inp		= "user_inp";
	
	boolean key_pressed = false;
	
	/*
	 * Audio descr. in this class
	 * 		Considering the structure of passing (AudioHandler) game_audio, it gave a lot of issues when clearing the leaderboard.
	 *  	This class (and game_audio) needs to know if the leaderboard is cleared or not to properly end the check_for_audio_status.
	 *  	The variable leaderboard_status will be either default or user_inp (when class is entered from StartMenu or GamePanel respectively),
	 *  	or cleared (when 'Clear Leaderboard' button is pressed)
	 */
	public EndMenu(JFrame frame, JLabel top, AudioHandler game_audio, String final_time_str, String top_text, String passing_leaderboard_status)
	{
		super();
			this.width  = frame.getWidth();
			this.height = (9 * frame.getHeight()) / 10;
		setPreferredSize(new Dimension(this.width, this.height));
		setLocation(0, 0);
		setBackground(blue);
		frame.add(this);
		top.setText(top_text);
		
		this.frame			= frame;
		this_game_audio		= game_audio;
		this.top			= top;
		leaderboard_status 	= passing_leaderboard_status;
		
		if(!final_time_str.equals(""))
			initUserInpGUI(final_time_str);
		else
			initEndPanel(null, "");
		
		Timer check_for_audio_status = new Timer();
		TimerTask task = new TimerTask()
		{
			@Override
			public void run()
			{
				if(this_game_audio.isAudio_finished())
				{
					int current_audio_index = this_game_audio.getCurrent_audio_index();
					this_game_audio = new AudioHandler("", current_audio_index);
					
					// Update buttons only after initEndPanel() has been called, i.e. after buttons has been init.
					if(!leaderboard_status.equals(string_user_inp))
					{						
						menu_btn.setAudioHandler(this_game_audio);
						clear_leaderboard_btn.setAudioHandler(this_game_audio);
						
						// Case when leaderboard HAS NOT been cleared
						if(leaderboard_status.equals(leaderboard_default))
							this_game_audio.setParent_frame(this_game_audio.string_Leaderboard);
						// Case when leaderboard HAS been cleared
						else if(leaderboard_status.equals(leaderboard_cleared))
							this_game_audio.setParent_frame(this_game_audio.string_Leaderboard_cleared);
					}
				}
				
				// End timer if leaderboard HAS NOT been cleared
				if(leaderboard_status.equals(leaderboard_default) &&
						!this_game_audio.getParent_frame().equals(this_game_audio.string_Leaderboard))
					check_for_audio_status.cancel();
				// End timer if leaderboard HAS been cleared
				if(leaderboard_status.equals(leaderboard_cleared) &&
						!this_game_audio.getParent_frame().equals(this_game_audio.string_Leaderboard_cleared))
					check_for_audio_status.cancel();
			}
		};
		check_for_audio_status.scheduleAtFixedRate(task, 0, 500);
	}
	
	// Name-input panel
	private void initUserInpGUI(String final_time_str)
	{
		// Assign status and game_audio parent_frame
		leaderboard_status = string_user_inp;
		this_game_audio.setParent_frame(this_game_audio.string_EndMenu);
		
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
		// Assign status and game_audio parent_frame
		if(leaderboard_status.equals(string_user_inp))
			leaderboard_status = leaderboard_default;
		if(leaderboard_status.equals(leaderboard_default))
			this_game_audio.setParent_frame(this_game_audio.string_Leaderboard);
		if(leaderboard_status.equals(leaderboard_cleared))
			this_game_audio.setParent_frame(this_game_audio.string_Leaderboard_cleared);
		
		String[] leaderboard_matrix = null;
		int leaderboard_len = 0;
		
		if(!final_time_str.equals(""))
		{			
			double tot_time = getTotTime(final_time_str);
			writeToLeaderboardMatrix(user_name, final_time_str, tot_time);			
		}
		
		leaderboard_matrix = getLeaderboardMatrix();
		if(leaderboard_matrix != null)
		{
			leaderboard_matrix = sortLeaderboardMatrix(leaderboard_matrix);
			leaderboard_len = leaderboard_matrix.length;
		}
		
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
			String current_entry = "   ";
			current_entry += (i + 1);
			current_entry += ". ";
			if(i < leaderboard_len)
			{				
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
				
			}
			JLabel l = new JLabel(current_entry, JLabel.LEFT);
			l.setForeground(yellow);
			l.setBackground(blue);
			l.setFont(font);
			l.setOpaque(true);
			leaderboard_container.add(l);
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
			
			menu_btn = new MenuButton("Main Menu", 4, 4, 4, 4);
			menu_btn.setFrame(frame);
			menu_btn.setTop(top);
			menu_btn.setPanel(this);
			menu_btn.setAudioHandler(this_game_audio);
			rest.add(menu_btn);
			
			JPanel filler_mid = new JPanel();
			filler_mid.setBackground(blue);
			rest.add(filler_mid);
			
			clear_leaderboard_btn = new MenuButton("Clear Leaderboard", 4, 4, 4, 4);
			clear_leaderboard_btn.setFrame(frame);
			clear_leaderboard_btn.setTop(top);
			clear_leaderboard_btn.setPanel(this);
			clear_leaderboard_btn.setAudioHandler(this_game_audio);
			rest.add(clear_leaderboard_btn);
			
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
		try
		{
			writer = new BufferedWriter(new FileWriter("leaderboard.txt", true));
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
		
		File file = new File("leaderboard.txt");
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
		reader.close();
		
		return leaderboard;
	}

	// Prepare leaderboard for sorting with respect to time
	private String[] sortLeaderboardMatrix(String[] matrix)
	{
		if(matrix == null)
			return null;
		
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

	// Sort based on time, lowest --> highest
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