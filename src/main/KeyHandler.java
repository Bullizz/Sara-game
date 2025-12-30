package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

public class KeyHandler implements KeyListener
{
//	JFrame frame;
	boolean game_paused = false;
	public boolean UP, LEFT, DOWN, RIGHT;
	int[] direction_arr = {0, 0};
	
	int slusk_points;
	boolean space_pressed;
	boolean slusk_active;
	
	boolean attila_active;
	char current_key;

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
		
		// For slusk minigame
		if(slusk_active)
		{
			if(user_inp == KeyEvent.VK_SPACE && !space_pressed)
			{
				space_pressed = true;
				slusk_points++;
			}
		}
		
		else if(attila_active)
		{
			int user_key = press.getKeyCode();
			switch(user_key)
			{
				case KeyEvent.VK_W:
					setCurrent_key('w');
					break;
				case KeyEvent.VK_A:
					setCurrent_key('a');
					break;
				case KeyEvent.VK_S:
					setCurrent_key('s');
					break;
				case KeyEvent.VK_D:
					setCurrent_key('d');
					break;
			}
		}
		
		else
		{
			if((user_inp == KeyEvent.VK_UP || user_inp == KeyEvent.VK_W))// && !DOWN)
			{
				UP = true;
//				DOWN = false;
				dy = -1;
			}
			if((user_inp == KeyEvent.VK_LEFT || user_inp == KeyEvent.VK_A))// && !RIGHT)
			{
				LEFT = true;
//				RIGHT = false;
				dx = -1;
			}
			if((user_inp == KeyEvent.VK_DOWN || user_inp == KeyEvent.VK_S))// && !UP)
			{
				DOWN = true;
//				UP = false;
				dy = 1;
			}
			if((user_inp == KeyEvent.VK_RIGHT || user_inp == KeyEvent.VK_D))// && !LEFT)
			{
				RIGHT = true;
//				LEFT = false;
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
		
		if(slusk_active)
		{			
			if(release.getKeyCode() == KeyEvent.VK_SPACE && space_pressed)
				space_pressed = false;
		}
		
		else if(attila_active)
		{
			setCurrent_key('\0');
		}
		
		else
		{
			if(user_inp == KeyEvent.VK_UP || user_inp == KeyEvent.VK_W)
			{
				UP = false;
				if(!DOWN)
					dy = -1;
			}
			if(user_inp == KeyEvent.VK_LEFT || user_inp == KeyEvent.VK_A)
			{
				LEFT = false;
				if(!RIGHT)
					dx = -1;
			}
			if(user_inp == KeyEvent.VK_DOWN || user_inp == KeyEvent.VK_S)
			{
				DOWN = false;
				if(!UP)
					dy = 1;
			}
			if(user_inp == KeyEvent.VK_RIGHT || user_inp == KeyEvent.VK_D)
			{
				RIGHT = false;
				if(!LEFT)
					dx = 1;
			}
			
			setDirection_arr(new int[]{dx, dy});
		}
	}

	public int[] getDirection_arr()
	{
		return direction_arr;
	}
	public void setDirection_arr(int[] direction_arr)
	{
		this.direction_arr = direction_arr;
	}
	
	// Slusk minigame methods
	public boolean isSpace_pressed()
	{
		return space_pressed;
	}
	public void setSpace_pressed(boolean space_pressed)
	{
		this.space_pressed = space_pressed;
	}

	public int getSlusk_points()
	{
		return slusk_points;
	}
	public void setSlusk_points(int slusk_points)
	{
		this.slusk_points = slusk_points;
	}
	
	public boolean isSlusk_active()
	{
		return slusk_active;
	}
	public void setSlusk_active(boolean slusk_active)
	{
		this.slusk_active = slusk_active;
	}
	
	// Attila minigame methods
	public boolean isAttila_active()
	{
		return attila_active;
	}
	public void setAttila_active(boolean attila_active)
	{
		this.attila_active = attila_active;
	}
	
	public char getCurrent_key()
	{
		return current_key;
	}
	public void setCurrent_key(char current_key)
	{
		this.current_key = current_key;
	}	
}
