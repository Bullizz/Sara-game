package main;

import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;

public class GameTimer extends JLabel
{
	Timer timer;
	TimerTask timer_task;
	double total_time = 0;
	double random_speed_coeff;
	int time_coeff;
	
	String time_str;

//	GAME_TIMER()
//	{
//		
//	}
	
	// Game timer that's displayed in top panel
	public void initTimer(JLabel top)
	{
		timer = new Timer();
		
		timer_task = new TimerTask()
		{			
			StringBuffer time_str_buffer;
			@Override
			public void run()
			{
				time_str_buffer = new StringBuffer();
				double HH, MM, SS, sub_SS;
				
				// Get how much time to increase/decrease with
				int time_coeff = getTime_coeff();
				total_time += time_coeff;
				
				// Change direction of entity with follow-type 3, every second
				if(total_time % 100 == 0)
					setRandom_speed_coeff();
				
				double time = total_time;
				
				// Get hours
				time /= 360000;
				HH = Math.floor(time);
				time -= HH;
				time *= 60;
				if(HH < 10)
					time_str_buffer.append("0");
				time_str_buffer.append((int) HH);
				time_str_buffer.append(":");
				
				// Get minutes
				MM = Math.floor(time);
				time -= MM;
				time *= 60;
				if(MM < 10)
					time_str_buffer.append("0");
				time_str_buffer.append((int) MM);
				time_str_buffer.append(":");
				
				// Get seconds
				SS = Math.floor(time);
				time -= SS;
				time *= 100;
				if(SS < 10)
					time_str_buffer.append("0");
				time_str_buffer.append((int) SS);
				time_str_buffer.append(".");
				
				// Get centi-seconds
				if(time >= 99)
					sub_SS = Math.floor(time);
				else
					sub_SS = Math.round(time);
				if(sub_SS < 10)
					time_str_buffer.append("0");
				time_str_buffer.append((int) sub_SS);
				
				if(time_str != null)
				{
//					System.out.println(time_str_buffer.toString());
					setTime_str(time_str_buffer.toString());
					top.setText(time_str_buffer.toString());
				}
				else
					setTime_str("00:00:00.00");
			}
		};
		timer.scheduleAtFixedRate(timer_task, 0, 10);
	}

	public int getTime_coeff()
	{
		return time_coeff;
	}
	public void setTime_coeff(int time_coeff)
	{
		this.time_coeff = time_coeff;
	}
	
	public float getRandom_speed_coeff()
	{
		return (float) random_speed_coeff;
	}
	public void setRandom_speed_coeff()
	{
		random_speed_coeff = (Math.random() * (1 - (-1))) + (-1);
	}

	public String getTime_str()
	{
		return time_str;
	}
	public void setTime_str(String time_str)
	{
		this.time_str = time_str;
	}
}