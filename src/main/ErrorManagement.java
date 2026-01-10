package main;

import javax.swing.JOptionPane;

public class ErrorManagement
{
	public ErrorManagement(String message, String contd_message)
	{
		String[] optns = {"More Info", "Close & Exit"}; 
		int cont_optn = JOptionPane.showOptionDialog(null,
													message,
													"Error",
													JOptionPane.CLOSED_OPTION,
													JOptionPane.ERROR_MESSAGE,
													null,
													optns,
													optns[1]);
		
		// More info option
		if(cont_optn == 0)
		{
			String message_2 = contd_message;
			message_2 = "<html>" + message_2 + "</html>";
			String[] optns_2 = {"Close & Exit"};
			
			int cont_optn_2 = JOptionPane.showOptionDialog(null,
					message_2,
					"Error",
					JOptionPane.CLOSED_OPTION,
					JOptionPane.ERROR_MESSAGE,
					null,
					optns_2,
					optns_2[0]);
			
			if(cont_optn_2 == 0)
				System.exit(0);
		}
		// Exit game
		else if(cont_optn == 1 || cont_optn == -1)
			System.exit(0);
	}
}