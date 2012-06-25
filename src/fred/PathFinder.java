package fred;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fred.algorithm.AStar;
import fred.algorithm.Dijkstra;

public class PathFinder extends JFrame {

	private static final long serialVersionUID = -7472533349360456696L;

	private static final Map<String, Algorithm> ALGORITHMS = new HashMap<String, Algorithm>();
	public static final String A_STAR_MANHATTAN = "A-Star (Manhattan Distance)";
	public static final String A_STAR_CHEBYSHEV = "A-Star (Chebyshev Distance)";
	public static final String A_STAR_EUCLIDEAN = "A-Star (Euclidean Distance)";
	public static final String DIJKSTRA = "Dijkstra";
	public static final String BREADTH_FIRST_SEARCH = "Breadth-First Search";
	public static final String BIDIRECTIONAL_BREADTH_FIRST_SEARCH = "Bidirectional Breadth-First Search";

	static {
		ALGORITHMS.put(A_STAR_MANHATTAN, new AStar(AStar.MANHATTAN_DISTANCE));
		ALGORITHMS.put(A_STAR_CHEBYSHEV, new AStar(AStar.CHEBYSHEV_DISTANCE));
		ALGORITHMS.put(A_STAR_EUCLIDEAN, new AStar(AStar.EUCLIDEAN_DISTANCE));
		ALGORITHMS.put(DIJKSTRA, new Dijkstra());
	}

	private final JSpinner spnrWidth;
	private final JSpinner spnrHeight;
	private final Display display;
	private final JLabel lblTotalIterations;
	private final JSlider speedSlider;
	private final JButton btnClearWalls;
	private final JButton btnStart;
	private final JButton btnReset;

	private static int width = 50;
	private static int height = 30;
	private static Node source = new Node(1, 1);
	private static Node target = new Node(2, 2);
	private static final Set<Node> walls = new HashSet<Node>();

	private Thread loop;
	private boolean editing = true;
	private boolean running = false;
	private int iterations = 0;

	private Algorithm algorithm = ALGORITHMS.get(A_STAR_MANHATTAN);
	private final JLabel lblAlgorithm;

	private enum Clicked {
		SOURCE, TARGET, DELETE_WALL, ADD_WALL;
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					PathFinder frame = new PathFinder();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	private PathFinder() {
		setTitle("Path Finder");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gbl_contentPane.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 0.0, 1.0, 0.0, 0.0, 0.0,
				0.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 0.0, 1.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		JComboBox cmbbxAlgorithm = new JComboBox(new String[] {
				A_STAR_MANHATTAN, A_STAR_CHEBYSHEV, A_STAR_EUCLIDEAN, DIJKSTRA,
				BREADTH_FIRST_SEARCH, BIDIRECTIONAL_BREADTH_FIRST_SEARCH });
		cmbbxAlgorithm.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					resetAlgorithm();
					algorithm = ALGORITHMS.get(e.getItem());
				}
			}
		});

		lblAlgorithm = new JLabel("Algorithm:");
		GridBagConstraints gbc_lblAlgorithm = new GridBagConstraints();
		gbc_lblAlgorithm.insets = new Insets(0, 0, 5, 5);
		gbc_lblAlgorithm.anchor = GridBagConstraints.EAST;
		gbc_lblAlgorithm.gridx = 0;
		gbc_lblAlgorithm.gridy = 0;
		contentPane.add(lblAlgorithm, gbc_lblAlgorithm);

		GridBagConstraints gbc_cmbbxAlgorithm = new GridBagConstraints();
		gbc_cmbbxAlgorithm.anchor = GridBagConstraints.NORTH;
		gbc_cmbbxAlgorithm.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbbxAlgorithm.insets = new Insets(0, 0, 5, 5);
		gbc_cmbbxAlgorithm.gridx = 1;
		gbc_cmbbxAlgorithm.gridy = 0;
		contentPane.add(cmbbxAlgorithm, gbc_cmbbxAlgorithm);

		JLabel lblWidth = new JLabel("Width:");
		GridBagConstraints gbc_lblWidth = new GridBagConstraints();
		gbc_lblWidth.insets = new Insets(0, 0, 5, 5);
		gbc_lblWidth.gridx = 2;
		gbc_lblWidth.gridy = 0;
		contentPane.add(lblWidth, gbc_lblWidth);

		spnrWidth = new JSpinner(new SpinnerNumberModel(50, 10, 100, 1));
		spnrWidth.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				width = (Integer) spnrWidth.getValue();
				display.updateSize();
			}
		});
		GridBagConstraints gbc_spnrWidth = new GridBagConstraints();
		gbc_spnrWidth.insets = new Insets(0, 0, 5, 5);
		gbc_spnrWidth.gridx = 3;
		gbc_spnrWidth.gridy = 0;
		contentPane.add(spnrWidth, gbc_spnrWidth);

		JLabel lblHeight = new JLabel("Height:");
		GridBagConstraints gbc_lblHeight = new GridBagConstraints();
		gbc_lblHeight.insets = new Insets(0, 0, 5, 5);
		gbc_lblHeight.gridx = 4;
		gbc_lblHeight.gridy = 0;
		contentPane.add(lblHeight, gbc_lblHeight);

		spnrHeight = new JSpinner(new SpinnerNumberModel(30, 10, 100, 1));
		spnrHeight.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				height = (Integer) spnrHeight.getValue();
				display.updateSize();
			}
		});
		GridBagConstraints gbc_spnrHeight = new GridBagConstraints();
		gbc_spnrHeight.insets = new Insets(0, 0, 5, 0);
		gbc_spnrHeight.gridx = 5;
		gbc_spnrHeight.gridy = 0;
		contentPane.add(spnrHeight, gbc_spnrHeight);

		display = new Display();
		JScrollPane scrollPane = new JScrollPane(display);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 6;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		contentPane.add(scrollPane, gbc_scrollPane);

		JLabel lblDirections = new JLabel(
				"<html>Drag Red block to set source. Drag Green block to set target. Click and drag mouse to add or delete walls</html>");
		GridBagConstraints gbc_lblDirections = new GridBagConstraints();
		gbc_lblDirections.gridwidth = 6;
		gbc_lblDirections.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblDirections.insets = new Insets(0, 0, 5, 0);
		gbc_lblDirections.gridx = 0;
		gbc_lblDirections.gridy = 2;
		contentPane.add(lblDirections, gbc_lblDirections);

		lblTotalIterations = new JLabel("Total Iterations: 0");
		GridBagConstraints gbc_lblTotalIterations = new GridBagConstraints();
		gbc_lblTotalIterations.anchor = GridBagConstraints.WEST;
		gbc_lblTotalIterations.gridwidth = 2;
		gbc_lblTotalIterations.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalIterations.gridx = 0;
		gbc_lblTotalIterations.gridy = 3;
		contentPane.add(lblTotalIterations, gbc_lblTotalIterations);

		btnClearWalls = new JButton("Clear Walls");
		btnClearWalls.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				walls.clear();
				display.repaint();
			}
		});
		GridBagConstraints gbc_btnClearWalls = new GridBagConstraints();
		gbc_btnClearWalls.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnClearWalls.gridwidth = 4;
		gbc_btnClearWalls.insets = new Insets(0, 0, 5, 0);
		gbc_btnClearWalls.gridx = 2;
		gbc_btnClearWalls.gridy = 3;
		contentPane.add(btnClearWalls, gbc_btnClearWalls);

		JLabel lblSpeed = new JLabel("Speed:");
		GridBagConstraints gbc_lblSpeed = new GridBagConstraints();
		gbc_lblSpeed.anchor = GridBagConstraints.EAST;
		gbc_lblSpeed.insets = new Insets(0, 0, 0, 5);
		gbc_lblSpeed.gridx = 0;
		gbc_lblSpeed.gridy = 4;
		contentPane.add(lblSpeed, gbc_lblSpeed);

		speedSlider = new JSlider();
		speedSlider.setPaintLabels(true);
		speedSlider.setMinorTickSpacing(5);
		speedSlider.setMajorTickSpacing(10);
		speedSlider.setValue(20);
		speedSlider.setPaintTicks(true);
		speedSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				loop.interrupt();
			}
		});
		GridBagConstraints gbc_slider = new GridBagConstraints();
		gbc_slider.fill = GridBagConstraints.HORIZONTAL;
		gbc_slider.insets = new Insets(0, 0, 0, 5);
		gbc_slider.gridx = 1;
		gbc_slider.gridy = 4;
		contentPane.add(speedSlider, gbc_slider);

		btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String text = btnStart.getText();
				if (text.equals("Start")) {
					synchronized (algorithm) {
						algorithm.init();
						editing = false;
						running = true;
					}
					btnClearWalls.setEnabled(false);
					btnReset.setEnabled(true);
					btnStart.setText("Pause");
				} else if (text.equals("Pause")) {
					running = false;
					btnStart.setText("Continue");
				} else {
					running = true;
					btnStart.setText("Pause");
				}
			}
		});
		GridBagConstraints gbc_btnStart = new GridBagConstraints();
		gbc_btnStart.gridwidth = 2;
		gbc_btnStart.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnStart.insets = new Insets(0, 0, 0, 5);
		gbc_btnStart.gridx = 2;
		gbc_btnStart.gridy = 4;
		contentPane.add(btnStart, gbc_btnStart);

		btnReset = new JButton("Reset");
		btnReset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resetAlgorithm();
			}
		});
		btnReset.setEnabled(false);
		GridBagConstraints gbc_btnReset = new GridBagConstraints();
		gbc_btnReset.gridwidth = 2;
		gbc_btnReset.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnReset.gridx = 4;
		gbc_btnReset.gridy = 4;
		contentPane.add(btnReset, gbc_btnReset);

		Dimension size = new Dimension(600, 400);
		setPreferredSize(size);
		setMinimumSize(size);
		pack();
		setLocationRelativeTo(null);

		runLoop();
	}

	private void resetAlgorithm() {
		synchronized (algorithm) {
			editing = true;
			running = false;
			algorithm.reset();
		}
		display.repaint();
		btnClearWalls.setEnabled(true);
		btnReset.setEnabled(false);
		btnStart.setEnabled(true);
		btnStart.setText("Start");
		iterations = 0;
		updateIterations();
	}

	private void runLoop() {
		loop = new Thread() {
			@Override
			public void run() {
				for (;;) {
					synchronized (algorithm) {
						if (running) {
							if (algorithm.step()) {
								running = false;
								btnStart.setEnabled(false);
							}
							iterations++;
							display.repaint();
							updateIterations();
						}
					}
					long startWait = System.currentTimeMillis();
					long sleepTime = getSleepTime();
					do {
						try {
							Thread.sleep(sleepTime);
						} catch (InterruptedException ie) {
						}
						sleepTime = getSleepTime() - System.currentTimeMillis()
								+ startWait;
					} while (sleepTime > 0);
				}
			}
		};
		loop.start();
	}

	private long getSleepTime() {
		int speed = speedSlider.getValue();
		if (speed == 0) {
			return Long.MAX_VALUE;
		} else {
			return 1000 / speedSlider.getValue();
		}
	}

	private void updateIterations() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				lblTotalIterations.setText("Total Iterations: " + iterations);
			}
		});
	}

	public static Node getSource() {
		return source;
	}

	public static Node getTarget() {
		return target;
	}

	public static <E extends Node> List<E> getTraversable(E node) {
		List<E> traversable = new LinkedList<E>();
		for (int dX = -1; dX <= 1; dX++) {
			for (int dY = -1; dY <= 1; dY++) {
				if ((dX == 0 && dY == 0) || (dX < 0 && node.x == 0)
						|| (dX > 0 && node.x == width - 1)
						|| (dY < 0 && node.y == 0)
						|| (dY > 0 && node.y == height - 1)) {
					continue;
				}
				@SuppressWarnings("unchecked")
				E n = (E) node.newNode(node.x + dX, node.y + dY);
				if (!walls.contains(n)
						&& (dX == 0 || dY == 0 || (!walls.contains(new Node(
								node.x + dX, node.y)) && !walls
								.contains(new Node(node.x, node.y + dY))))) {
					traversable.add(n);
				}
			}
		}
		return traversable;
	}

	private class Display extends JComponent implements MouseListener,
			MouseMotionListener {

		private static final long serialVersionUID = -3523892746907495075L;

		private static final int BOX_WIDTH = 30;
		private static final int BOX_MID = BOX_WIDTH / 2;

		private final Stroke stroke = new BasicStroke(3, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);

		private Dimension size;

		public Display() {
			addMouseListener(this);
			addMouseMotionListener(this);
			updateSize();
		}

		public void updateSize() {
			if (source.x >= width) {
				source = new Node(width - 1, source.y);
			}
			if (source.y >= height) {
				source = new Node(source.x, height - 1);
			}
			if (target.x >= width) {
				target = new Node(width - 1, target.y);
			}
			if (target.y >= height) {
				target = new Node(target.x, height - 1);
			}
			size = new Dimension(width * BOX_WIDTH + 1, height * BOX_WIDTH + 1);
			setPreferredSize(size);
			setSize(size);
			repaint();
		}

		@Override
		public void paint(Graphics g1) {
			super.paint(g1);
			Graphics2D g = (Graphics2D) g1;
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);

			List<Node> closed = copy(algorithm.getVisitedNodes());
			List<Node> open = copy(algorithm.getUnvisitedNodes());

			g.setColor(Color.GRAY);
			drawNodes(walls, g);

			g.setColor(Color.BLUE);
			drawNodes(closed, g);

			g.setColor(Color.CYAN);
			drawNodes(open, g);

			g.setColor(Color.GREEN);
			drawNode(target, g);

			g.setColor(Color.RED);
			drawNode(source, g);

			g.setColor(Color.LIGHT_GRAY);
			for (int i = 0; i <= width; i++) {
				int x = i * BOX_WIDTH;
				g.drawLine(x, 0, x, 0 + size.height);
			}
			for (int i = 0; i <= height; i++) {
				int y = i * BOX_WIDTH;
				g.drawLine(0, y, 0 + size.width, y);
			}

			g.setStroke(stroke);
			g.setColor(Color.GRAY);
			drawLinks(closed, g);
			drawLinks(open, g);

			g.setColor(Color.YELLOW);
			for (Node node = algorithm.getCurrentNode(); node != null; node = node.parent) {
				drawLink(node, g);
			}
		}

		private List<Node> copy(Collection<? extends Node> nodes) {
			return new LinkedList<Node>(nodes);
		}

		private void drawNodes(Collection<Node> nodes, Graphics g) {
			for (Node node : nodes) {
				drawNode(node, g);
			}
		}

		private void drawNode(Node node, Graphics g) {
			if (isWithinBounds(node)) {
				int x = node.x * BOX_WIDTH;
				int y = node.y * BOX_WIDTH;
				g.fillRect(x, y, BOX_WIDTH, BOX_WIDTH);
			}
		}

		private void drawLinks(List<Node> nodes, Graphics g) {
			for (Node node : nodes) {
				drawLink(node, g);
			}
		}

		private void drawLink(Node node, Graphics g) {
			if (node.parent != null && isWithinBounds(node)) {
				int x1 = node.x * BOX_WIDTH + BOX_MID;
				int y1 = node.y * BOX_WIDTH + BOX_MID;
				int x2 = node.parent.x * BOX_WIDTH + BOX_MID;
				int y2 = node.parent.y * BOX_WIDTH + BOX_MID;
				g.drawLine(x1, y1, x2, y2);
			}
		}

		private boolean isWithinBounds(Node node) {
			return node.x >= 0 && node.x < width && node.y >= 0
					&& node.y < height;
		}

		private Clicked clicked;
		private Node mouseLocation;

		@Override
		public void mousePressed(MouseEvent e) {
			if (editing) {
				mouseLocation = pointToNode(e.getPoint());
				if (mouseLocation.equals(source)) {
					clicked = Clicked.SOURCE;
				} else if (mouseLocation.equals(target)) {
					clicked = Clicked.TARGET;
				} else {
					if (walls.contains(mouseLocation)) {
						clicked = Clicked.DELETE_WALL;
						walls.remove(mouseLocation);
					} else {
						clicked = Clicked.ADD_WALL;
						walls.add(mouseLocation);
					}
					repaint();
				}
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (editing) {
				Node loc = pointToNode(e.getPoint());
				if (!loc.equals(mouseLocation)) {
					if (clicked == Clicked.SOURCE) {
						source = loc;
					} else if (clicked == Clicked.TARGET) {
						target = loc;
					} else if (clicked == Clicked.DELETE_WALL) {
						walls.remove(loc);
					} else {
						walls.add(loc);
					}
					mouseLocation = loc;
					repaint();
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (editing) {
				clicked = null;
				mouseLocation = null;
			}
		}

		private Node pointToNode(Point point) {
			int x = point.x / BOX_WIDTH;
			int y = point.y / BOX_WIDTH;
			return new Node(x, y);
		}

		@Override
		public void mouseMoved(MouseEvent e) {
		}

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}
	}
}
