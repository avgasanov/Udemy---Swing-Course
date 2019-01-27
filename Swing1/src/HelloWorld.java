
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import gui.MainFrame;

public class HelloWorld {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame firstFrame = new MainFrame();
				
			}
		});
		
	}

}
