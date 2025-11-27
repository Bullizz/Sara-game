package main;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class MouseList implements /*MouseListener, */MouseMotionListener 
{
	int[][] map_constraints = new int[10000][2];
	int map_constraints_index = 0;
	boolean mouse_moving = false;
	int[][] matrix;
	int top_diff;
	
	public MouseList(int width, int height, int top_diff)
	{
		matrix = new int[height][width];
		for(int i = 0; i < matrix.length; i++)
		{
			for(int j = 0; j < matrix[0].length; j++)
				matrix[i][j] = 0; 
		}
		this.top_diff = top_diff;
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		int x = e.getX();
		int y = e.getY() - top_diff;
		matrix[y][x] = 1;
//		System.out.println(x + ", " + y);
//		System.out.println(map_constraints_index);
	}

	@Override
	public void mouseMoved(MouseEvent e)
	{
		if(e.getX() == 0 && e.getY() == 0)
		{
			BufferedWriter writer;
			try
			{			
				File file = new File("map-1.txt");
				if(!file.exists())
					file.createNewFile();
				
				writer = new BufferedWriter(new FileWriter("map-1.txt", false));
				for(int i = 0; i < matrix.length; i++)
				{
					for(int j = 0; j < matrix[0].length; j++)
					{
						if(matrix[i][j] == 0)
							writer.append('0');
						else if(matrix[i][j] == 1)
							writer.append('1');
						
//						writer.append(' ');
					}
					writer.append("\n");
				}
				writer.close();
			}
			catch(Exception ex)
			{
				System.err.println("Gen. file");
			}
		}
	}
}
