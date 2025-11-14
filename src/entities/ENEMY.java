package entities;

public class ENEMY
{
	public int x, y;

	final public int width, height;
	final public String id_string;
	
	public final int max_speed = 8;
	
	public ENEMY(String id_string, int follow_type, int x, int y, int width, int height)
	{
		this.id_string = id_string;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public int getEnemy_x()
	{
		return x;
	}
	public void setEnemy_x(int enemy_x)
	{
		this.x = enemy_x;
	}
	
	public int getEnemy_y()
	{
		return y;
	}
	
	public void setEnemy_y(int enemy_y)
	{		
		this.y = enemy_y;
	}
		
}