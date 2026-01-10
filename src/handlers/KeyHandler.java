package handlers;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

public class KeyHandler implements KeyListener
{
	public boolean GamePanel_esc_pressed = false;
	public boolean UP, LEFT, DOWN, RIGHT;
	int[] direction_arr = {0, 0};
	
	// Slusk minigame
	int slusk_points;
	boolean slusk_space_pressed;
	boolean slusk_active;
	
	// Attila minigame
	boolean attila_active;
	char current_key;
	
	@Override
	public void keyTyped(KeyEvent e){}
	@Override
	public void keyPressed(KeyEvent press)
	{
		int user_inp = press.getKeyCode();
		int dx = getDirection_arr()[0];
		int dy = getDirection_arr()[1];
		
		if(user_inp == KeyEvent.VK_ESCAPE)
			GamePanel_esc_pressed = true;
		
		// For slusk minigame
		if(slusk_active)
		{
			if(user_inp == KeyEvent.VK_SPACE && !slusk_space_pressed)
			{
				slusk_space_pressed = true;
				slusk_points++;
			}
		}

		// For attila minigame
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
				dy = -1;
			}
			if((user_inp == KeyEvent.VK_LEFT || user_inp == KeyEvent.VK_A))// && !RIGHT)
			{
				LEFT = true;
				dx = -1;
			}
			if((user_inp == KeyEvent.VK_DOWN || user_inp == KeyEvent.VK_S))// && !UP)
			{
				DOWN = true;
				dy = 1;
			}
			if((user_inp == KeyEvent.VK_RIGHT || user_inp == KeyEvent.VK_D))// && !LEFT)
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
		
		if(slusk_active)
		{			
			if(release.getKeyCode() == KeyEvent.VK_SPACE && slusk_space_pressed)
				slusk_space_pressed = false;
		}
		
		else if(attila_active)
		{
			switch(current_key)
			{
				case 'w':
					if(release.getKeyCode() == KeyEvent.VK_W)
						setCurrent_key('\0');
					break;
				case 'a':
					if(release.getKeyCode() == KeyEvent.VK_A)
						setCurrent_key('\0');
					break;
				case 's':
					if(release.getKeyCode() == KeyEvent.VK_S)
						setCurrent_key('\0');
					break;
				case 'd':
					if(release.getKeyCode() == KeyEvent.VK_D)
						setCurrent_key('\0');
					break;
			}
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
		return slusk_space_pressed;
	}
	public void setSpace_pressed(boolean space_pressed)
	{
		this.slusk_space_pressed = space_pressed;
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