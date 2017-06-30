import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import com.fazecast.jSerialComm.SerialPort;


public class Main {
	static Date date = new Date() ;
	static SerialPort chosenPort;
	static int x = 0;

	public static void main(String[] args) {
		
		// create and configure the window
		JFrame window = new JFrame();
		window.setTitle("GM Datalogger GUI");
		window.setSize(1024, 768);
		window.setLayout(new BorderLayout());
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// create a drop-down box with active port list and connect button
		JComboBox<String> portList = new JComboBox<String>();
		JButton connectButton = new JButton("Connect");
		JPanel topPanel = new JPanel();
		topPanel.add(portList);
		topPanel.add(connectButton);
		window.add(topPanel, BorderLayout.NORTH);
		
		// add-in the list into the drop-down box
		SerialPort[] portNames = SerialPort.getCommPorts();
		for(int i = 0; i < portNames.length; i++)
			portList.addItem(portNames[i].getSystemPortName());
				
		// create the line graph
		XYSeries XYlinedataset = new XYSeries("count value");
		XYSeriesCollection dataset = new XYSeriesCollection(XYlinedataset);
		JFreeChart XYchart = ChartFactory.createXYLineChart("Count/time graph", "Interval No.", " Count No. ", dataset, PlotOrientation.VERTICAL,false,false,false);
		XYPlot chartCategoryPlot = XYchart.getXYPlot();
		chartCategoryPlot.setRangeGridlinePaint(Color.BLACK);
		chartCategoryPlot.setBackgroundPaint(Color.WHITE);
		chartCategoryPlot.setDomainGridlinePaint(Color.BLACK);
		// create histogram
		HashMap<Integer, Integer> histogramRawData = new HashMap<Integer, Integer>();
		DefaultCategoryDataset histogramDataset = new DefaultCategoryDataset();
		JFreeChart histogramChart = ChartFactory.createBarChart("Histogram", "Count value ", "Occurence No.", histogramDataset);
		CategoryPlot histogramCategoryPlot = histogramChart.getCategoryPlot();
		histogramCategoryPlot.setRangeGridlinePaint(Color.BLACK);
		histogramCategoryPlot.setBackgroundPaint(Color.WHITE);
		
		BarRenderer barrenderer = (BarRenderer) histogramCategoryPlot.getRenderer();
		barrenderer.setMaximumBarWidth(.05);
		barrenderer.setDrawBarOutline(false);
		
		// panel with graphs
		JPanel graphsPanel = new JPanel();
		graphsPanel.add(new ChartPanel(XYchart));
		graphsPanel.add(new ChartPanel(histogramChart));
		graphsPanel.setLayout(new GridLayout(2, 0));
		window.add(graphsPanel, BorderLayout.CENTER);
		
		// configure the connect button and use another thread to listen for data
		connectButton.addActionListener(new ActionListener(){
			@Override public void actionPerformed(ActionEvent arg0) {
				if(connectButton.getText().equals("Connect")) {
					// attempt to connect to the serial port
					chosenPort = SerialPort.getCommPort(portList.getSelectedItem().toString());
					chosenPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
					if(chosenPort.openPort()) {
						connectButton.setText("Disconnect");
						portList.setEnabled(false);
					}
					
					// create a new thread that listens for incoming text and adding ";" for separator
					Thread thread = new Thread(){
						@Override public void run() {
							Scanner scanner = new Scanner(chosenPort.getInputStream());
							while(scanner.hasNextLine()) {
								try {
									String line = scanner.nextLine();
									int number = Integer.parseInt(line);
									
									//Create or open if already exist txt file and adding the data
									SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss") ;
									File log = new File(dateFormat.format(date) + ".txt");
									if(log.exists()==false){
							            System.out.println("We had to make a new file.");
							            log.createNewFile();
							            }
									PrintWriter writer = new PrintWriter(new FileWriter(log, true));
									writer.print(number);
									writer.println(";");
									writer.close();
									// populates the graph and refreshing
									
									XYlinedataset.add( x++, number);
									
									
									int histogramActualValue;		//odczytywanie aktualnej wartości kolumnowej histogramu
									if (histogramRawData.containsKey(number))
									{
										histogramActualValue = histogramRawData.get(number);		//gdy juz sie pojawila wczesniej
									}
									else
									{
										histogramActualValue = 0;			//gdy pierwszy raz
									}
									
									histogramRawData.put(number, histogramActualValue + 1);		//iteracja ilosci pojawien o 1
									UpdateHistogramDataset(histogramDataset, histogramRawData);		//odswiezenie dataset

									window.repaint();
								} catch(Exception e) {}
							}
							scanner.close();
						}
					};
					thread.start();
				} else {
					// disconnect from the serial port and restarting graph
					chosenPort.closePort();
					portList.setEnabled(true);
					connectButton.setText("Connect");
					XYlinedataset.clear();
					histogramDataset.clear();
					x = 0;
				}
			}
		});
		
		// show the window
		window.setVisible(true);
	}

	
	private static void UpdateHistogramDataset(DefaultCategoryDataset dataset, HashMap<Integer, Integer> rawData)
	{
		dataset.clear();		//czyszczenie dataset
		
		//pobranie kluczy/biny i sortowanie
		Set<Integer> keysSet = rawData.keySet();		
		List<Integer> binList = new ArrayList<Integer>();
		binList.addAll(keysSet);
		Collections.sort(binList);
		
		//copy sorted dataset to histogram dataset
		for (int bin : binList)
		{
			Integer value = rawData.get(bin);
			dataset.setValue(value, "N of Counts", Integer.toString(bin));
		}
	}
}
