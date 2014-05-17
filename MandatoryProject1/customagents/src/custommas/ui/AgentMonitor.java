package custommas.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.ColorUIResource;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import custommas.agents.CustomAgent;

public class AgentMonitor {
	private JFrame mainFrame;
	private CustomTableModel agentTableModel;
	private JTable agentTable;
	
	private Thread updateThread;
	
	private HashMap<String, CustomAgent> _agents;
	private int _updatesHandled;
	private int _updatesRequested;
	
	public AgentMonitor(){
		_agents = new HashMap<String, CustomAgent>();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.put("Table.gridColor", new ColorUIResource(Color.LIGHT_GRAY));
			
			mainFrame = new JFrame("Agent Monitor");
			mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			agentTableModel = new CustomTableModel();
			agentTable = new JTable(agentTableModel);

			JScrollPane scrollPane = new JScrollPane(agentTable);
			agentTable.setPreferredScrollableViewportSize(new Dimension(600, 300));
			
			JButton updateButton = new JButton("Update");
			updateButton.addActionListener(new ActionListener(){
				//@Override
				public void actionPerformed(ActionEvent e) {
					update();
					updateThread.interrupt();
				}
			});
			
			mainFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);
			mainFrame.getContentPane().add(updateButton, BorderLayout.SOUTH);
			
			//mainFrame.pack();
			mainFrame.setMinimumSize(new Dimension(600, 300));
			mainFrame.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		updateThread = new Thread(new Runnable(){
			public void run(){
				while(true){
					try{
						if(_updatesRequested > _updatesHandled){
							_updatesHandled = _updatesRequested;
							updateGui();
						}
						Thread.sleep(500);
					}catch(Exception e){
						if(!(e instanceof InterruptedException)){
							e.printStackTrace();
						}
					}
				}
			}
		});
		updateThread.setDaemon(true);
		updateThread.start();
	}
	
	public void update(){
		_updatesRequested++;
	}
	
	private synchronized void updateGui(){
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				if(agentTableModel.getRowCount() < 1) return;
				int rows = agentTableModel.getRowCount();
				for(int row = 0; row < rows; row++){
					String agentName = (String)agentTableModel.getValueAt(row, 0);
					CustomAgent agent = _agents.get(agentName);
					agentTableModel.setValueAt(agent.getPosition(), row, 2, false);
					agentTableModel.setValueAt(agent.getHealth() + "/" + agent.getMaxHealth(), row, 3, false);
					agentTableModel.setValueAt(agent.getEnergy() + "/" + agent.getMaxEnergy(), row, 4, false);
					agentTableModel.setValueAt(agent.getPlannedAction(), row, 5, false);
					agentTableModel.setValueAt(agent.getLastAction(), row, 6, false);
				}
				agentTableModel.fireTableDataChanged();
			}
		});
	}
	
	public void registerAgent(final CustomAgent agent){
		if(agent == null || _agents.containsKey(agent.getName())) return;
		_agents.put(agent.getName(), agent);
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				agentTableModel.addRow(new Object[]{
					agent.getName(), agent.getRole(), agent.getPosition()
				});
			}
		});
	}
	
	private static AgentMonitor _instance;
	public static AgentMonitor getInstance(){
		if(_instance == null){
			_instance = new AgentMonitor();
		}
		return _instance;
	}
	
	class CustomTableModel extends AbstractTableModel {
		private final String[] columnNames = new String[]{
			"Name", "Role", "Position", "Health", "Energy", "Action", "Last Action"
		};
		
		private final Class[] columnTypes = new Class[]{
			String.class, String.class, String.class, String.class, String.class, String.class, String.class
		};

	    private final ArrayList<Object[]> data = new ArrayList<Object[]>();
	    
	    public CustomTableModel(){
	    	super();
	    }

	    public int getColumnCount() {
	    	return columnNames.length;
	    }

	    public int getRowCount() {
	    	return data.size();
	    }

	    public String getColumnName(int col) {
	    	return columnNames[col];
	    }

	    public Object getValueAt(int row, int col) {
	    	return data.get(row)[col];
	    }
	    
	    public void addRow(Object[] rowData){
	    	if(rowData.length != columnNames.length){
	    		Object[] oldRowData = rowData;
	    		rowData = new Object[columnNames.length];
	    		int toCopy = (int)Math.min(oldRowData.length, columnNames.length);
	    		System.arraycopy(oldRowData, 0, rowData, 0, toCopy);
	    	}
	    	
	    	int oldRowsAmount = data.size();
	    	data.add(rowData);
	    	fireTableRowsInserted(oldRowsAmount, oldRowsAmount);
	    }

	    public Class<?> getColumnClass(int c) {
	    	return columnTypes[c];
	    }

	    public boolean isCellEditable(int row, int col) {
	    	return false;
	    }
	    
	    public void setValueAt(Object value, int row, int col){
	    	setValueAt(value, row, col, true);
	    }
	    
	    public void setValueAt(Object value, int row, int col, boolean fireChangedEvent){
	    	data.get(row)[col] = value;
	    	if(fireChangedEvent){
	    		fireTableCellUpdated(row, col);
	    	}
	    }
	}
}
