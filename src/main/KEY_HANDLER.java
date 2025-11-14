package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

public class KEY_HANDLER implements KeyListener
{
	JFrame frame;
	boolean game_paused = false;
	public boolean UP, LEFT, DOWN, RIGHT;
	int[] direction_arr = {0, 0};
	
	KEY_HANDLER(JFrame frame)
	{
		this.frame = frame;
	}

	public boolean isGame_paused()
	{
		return game_paused;
	}
	public void setGame_paused(boolean game_paused)
	{
		this.game_paused = game_paused;
	}
	
	@Override
	public void keyTyped(KeyEvent e){}
	@Override
	public void keyPressed(KeyEvent press)
	{
		int user_inp = press.getKeyCode();
		int dx = getDirection_arr()[0];
		int dy = getDirection_arr()[1];
		
		// Toggle pause status
		if(user_inp == KeyEvent.VK_ESCAPE && !game_paused)
		{
			setGame_paused(true);
			System.exit(0);
		}
		else if(user_inp == KeyEvent.VK_ESCAPE && game_paused)
		{
			setGame_paused(false);
			System.exit(0);
		}
		
		else
		{
			if(user_inp == KeyEvent.VK_UP || user_inp == KeyEvent.VK_W)
			{
				UP = true;
				dy = -1;
			}
			if(user_inp == KeyEvent.VK_LEFT || user_inp == KeyEvent.VK_A)
			{
				LEFT = true;
				dx = -1;
			}
			if(user_inp == KeyEvent.VK_DOWN || user_inp == KeyEvent.VK_S)
			{
				DOWN = true;
				dy = 1;
			}
			if(user_inp == KeyEvent.VK_RIGHT || user_inp == KeyEvent.VK_D)
			{
				RIGHT = true;
				dx = 1;
			}
			
			setDirection_arr(new int[]{dx, dy});
		}
	}
	
	@Override
	public void keyReleased(KeyEvent release)
	{
		int user_inp = release.getKeyCode();
		int dx = getDirection_arr()[0];
		int dy = getDirection_arr()[1];
		
		if(user_inp == KeyEvent.VK_UP || user_inp == KeyEvent.VK_W)
		{
			UP = false;
			dy = -1;
		}
		if(user_inp == KeyEvent.VK_LEFT || user_inp == KeyEvent.VK_A)
		{
			LEFT = false;
			dx = -1;
		}
		if(user_inp == KeyEvent.VK_DOWN || user_inp == KeyEvent.VK_S)
		{
			DOWN = false;
			dy = 1;
		}
		if(user_inp == KeyEvent.VK_RIGHT || user_inp == KeyEvent.VK_D)
		{
			RIGHT = false;
			dx = 1;
		}
		
		setDirection_arr(new int[]{dx, dy});
	}

	public int[] getDirection_arr()
	{
		return direction_arr;
	}
	public void setDirection_arr(int[] direction_arr)
	{
		this.direction_arr = direction_arr;
	}
}
