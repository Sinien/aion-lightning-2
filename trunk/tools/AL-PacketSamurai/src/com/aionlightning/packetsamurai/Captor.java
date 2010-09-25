package com.aionlightning.packetsamurai;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javolution.util.FastMap;
import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;
import jpcap.PacketReceiver;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;

import com.aionlightning.packetsamurai.gui.ChoiceDialog;
import com.aionlightning.packetsamurai.protocol.Protocol;
import com.aionlightning.packetsamurai.protocol.ProtocolManager;

/**
 * 
 * @author Ulysses R. Ribeiro
 * @author Gilles Duboscq
 * 
 */
public class Captor implements Runnable {

	protected JpcapCaptor _packetCaptor;
	protected NetworkInterfaceAddress[] _networkAddress;
	private boolean _captorEnabled = false;
	private static final int SNAPSHOT_LENGHT = 4 * 65536;
	private int _deviceId = -1;
	protected PacketReceiver _packetHandler;
	protected static Map<Integer, Protocol> _activeProtocols;

	private static class SingletonHolder {
		private final static Captor singleton = Captor.initCaptor();
	}

	public static Captor getInstance() {
		return SingletonHolder.singleton;
	}

	private static Captor initCaptor() {
		try {
			Captor captor = new Captor();
			captor.initDevice();
			Thread captorThread = new Thread(captor, "CaptorThread");
			captorThread.setPriority(Thread.MAX_PRIORITY);
			captorThread.start();
			return captor;
		} catch (Exception e) {
			Captor.showSetActiveProtocols();

			// try to init again (will ask user to set procotol agains if he
			// canceled this previous one)
			return Captor.initCaptor();
		}
	}

	public Captor() {
		_packetHandler = new PacketHandler(this);
	}

	public void initDevice() throws Exception {
		this.configureProtocols();

		NetworkInterface[] niList = null;
		try {
			niList = JpcapCaptor.getDeviceList();
		} catch (UnsatisfiedLinkError ule) {
			PacketSamurai.getUserInterface().log(
					"ERROR: You are missing the JPcap lib :\n"
							+ ule.getLocalizedMessage());
			setCaptor(false);
			return;
		}
		int deviceCount = niList.length;
		// Check if there is a device to sniff
		if (deviceCount <= 0) {
			setCaptor(false);
			PacketSamurai.getUserInterface().log(
					"ERROR: No Network Interfaces have been found!");
		}
		// if there is only one we bind it
		else if (deviceCount == 1) {
			openDevice(niList[0]);
		} else {
			if (!PacketSamurai.configExists("NetworkInterface")) {
				selectNetWorkInterface();
			} else {
				int deviceNumber = Integer.parseInt(PacketSamurai
						.getConfigProperty("NetworkInterface"));
				openDevice(deviceNumber);
			}
		}
	}

	public void selectNetWorkInterface() {
		NetworkInterface niList[] = JpcapCaptor.getDeviceList();
		int deviceCount = niList.length;
		String nameList[] = new String[deviceCount];
		for (int i = 0; i < deviceCount; i++) {

			PacketSamurai.getUserInterface().log(
					"Found Interface: " + niList[i].name);
			nameList[i] = (niList[i].addresses.length >= 1 ? niList[i].addresses[0].address
					.getHostAddress()
					+ ": "
					: "")
					+ niList[i].name;
			if (niList[i].description != null)
				nameList[i] += String.format(" (%s)", niList[i].description);
		}
		String[][] choices = new String[1][];
		choices[0] = nameList;

		int[] ret = ChoiceDialog.choiceDialog("Select Interface for Sniffing",
				new String[] { "Interfaces" }, choices);
		if (ret != null) {
			this.openDevice(ret[0]);
			PacketSamurai.setConfigProperty("NetworkInterface", Integer
					.toString(ret[0]));
		} else {
			PacketSamurai.getUserInterface().log("No interface selected.");
		}
		// TODO delete its stuff
		// Main.getInstance().showInterfaceSelector(nameList);
	}

	public void openDevice(int deviceNumber) {
		openDevice(JpcapCaptor.getDeviceList()[deviceNumber]);
		_deviceId = deviceNumber;
	}

	private void configureProtocols() throws Exception {
		Captor.setActiveProtocols(PacketSamurai.loadSnifferActiveProtocols());
		if (Captor.getActiveProtocols() == null) {
			Captor.setActiveProtocols(new FastMap<Integer, Protocol>());

			for (Protocol p : ProtocolManager.getInstance().getProtocols()) {
				if (Captor.getActiveProtocols().containsKey(p.getPort())) {
					// invalidate the map being built
					Captor.setActiveProtocols(null);
					throw new Exception(
							"More then one protocol with same port, only one protocol per port can be active for the sniffer.");
				}
				Captor.getActiveProtocols().put(p.getPort(), p);
			}
		}

	}

	public void openDevice(NetworkInterface ni) {
		try {
			if (_packetCaptor != null) {
				setCaptor(false);
				_packetCaptor.close();
			}
			_packetCaptor = JpcapCaptor.openDevice(ni, SNAPSHOT_LENGHT, false,
					10);

			setCaptor(true);
			_networkAddress = ni.addresses;
			PacketSamurai.getUserInterface().log(
					"Successfully opened device (" + ni.name + ").");

			Set<Integer> ports = Captor.getActiveProtocols().keySet();
			Iterator<Integer> i = ports.iterator();

			String filter = PacketSamurai.getConfigProperty("filter", "")
					.trim();
			if (filter.length() > 0) {
				PacketSamurai.getUserInterface().log(
						"Sniffing with filter: " + filter);
				_packetCaptor.setFilter(filter, false);
			} else if (i.hasNext()) {
				StringBuilder sb = new StringBuilder("(tcp port");
				StringBuilder portsSB = new StringBuilder();
				for (; i.hasNext();) {
					Integer port = i.next();
					sb.append(" " + port + ")");
					portsSB.append(port);

					if (i.hasNext()) {
						portsSB.append(' ');
						sb.append(" or (tcp port");
					}
				}
				PacketSamurai.getUserInterface().log(
						"Sniffing with filter: " + sb.toString());
				_packetCaptor.setFilter(sb.toString(), false);
				PacketSamurai.getUserInterface().log(
						"Sniffing on port(s): " + portsSB);
			}
		} catch (IOException ioe) {
			PacketSamurai.getUserInterface().log(
					"ERROR: Failed to open device (" + ni.name
							+ ") for capture " + ioe);
		}
	}

	public void setCaptor(boolean val) {
		if (val == false && _captorEnabled == true) {
			_packetCaptor.breakLoop();
		}
		_captorEnabled = val;
	}

	public boolean isCaptorEnabled() {
		return _captorEnabled;
	}

	public void run() {
		if (!this.isCaptorEnabled())
			return;
		// capture packets indefinitely

		/*
		 * This may cause crashes with version 0.7 of jpcap
		 * _packetCaptor.loopPacket( -1, _packetHandler);
		 */

		// Seriously this is such a bad hack but i can't help it
		while (true) {
			Packet p = _packetCaptor.getPacket();

			if (p != null && p.data != null && p.data.length > 0) {
				_packetHandler.receivePacket(p);
			}

		}
	}

	public JpcapCaptor getPcapCaptor() {
		return _packetCaptor;
	}

	public boolean isClientAddress(InetAddress address) {
		for (int i = 0; i < _networkAddress.length; i++) {
			if (_networkAddress[i].address.equals(address))
				return true;
		}
		return false;
	}

	public int getCurrentDeviceId() {
		return _deviceId;
	}

	public static void setActiveProtocols(Map<Integer, Protocol> activeProtocols) {
		_activeProtocols = activeProtocols;
	}

	public static Protocol getActiveProtocolForPort(int port) {
		return Captor.getActiveProtocols().get(port);
	}

	protected static Map<Integer, Protocol> getActiveProtocols() {
		return _activeProtocols;
	}

	public String getPacketDump(TCPPacket tcpPacket) {
		StringBuffer sb = new StringBuffer();

		sb.append("Received a packet: " + PacketSamurai.hexDump(tcpPacket.data)
				+ " - Flags: ");
		sb.append(" - ACK: "
				+ (tcpPacket.ack ? "1 : " + tcpPacket.ack_num : "0"));
		sb.append(" - Fin: " + (tcpPacket.fin ? "1" : "0"));
		sb.append(" - SYN: " + (tcpPacket.syn ? "1" : "0"));
		sb.append(" - FIN: " + (tcpPacket.fin ? "1" : "0"));
		sb.append(" - RST: " + (tcpPacket.rst ? "1" : "0"));
		sb.append(" - Seq:" + tcpPacket.sequence);
		sb.append(" - "
				+ (this.isClientAddress(tcpPacket.src_ip) ? "C->S" : "S->C"));
		sb.append(" - psh: " + (tcpPacket.psh ? "1" : "0"));
		sb.append(" - Delay: " + (tcpPacket.d_flag ? "1" : "0"));
		sb.append(" - Dont Fragment: " + (tcpPacket.dont_frag ? "1" : "0"));
		sb.append(" - More Fragment: " + (tcpPacket.more_frag ? "1" : "0"));
		sb.append(" - Realibility: " + (tcpPacket.more_frag ? "1" : "0"));
		sb.append(" - Frag Reservation: " + (tcpPacket.rsv_frag ? "1" : "0"));

		return sb.toString();
	}

	public static void showSetActiveProtocols() {
		int total = ProtocolManager.getInstance().getProtocolsByPort().size();
		String[] titles = new String[total];
		String[][] choices = new String[total][];
		Protocol[][] protocols = new Protocol[total][];
		int i = 0;
		for (int port : ProtocolManager.getInstance().getProtocolsByPort()
				.keySet()) {
			Set<Protocol> prots = ProtocolManager.getInstance()
					.getProtocolForPort(port);
			titles[i] = "Port " + port;
			int count = prots.size();
			int j = 0;
			choices[i] = new String[count];
			protocols[i] = new Protocol[count];
			for (Protocol prot : prots) {
				protocols[i][j] = prot;
				choices[i][j++] = prot.getName();
			}
			i++;
		}
		PacketSamurai
				.getUserInterface()
				.log(
						"Please select the active protocols for Sniffing, non active protocols are used for opening old logs.");
		int[] ret = ChoiceDialog.choiceDialog(
				"Select Active Protocols for Sniffing", titles, choices);

		// u are doomed to properly set it
		if (ret != null) {
			Map<Integer, Protocol> activeProtocols = new FastMap<Integer, Protocol>();
			i = 0;
			for (int sel : ret) {
				Protocol p = protocols[i++][sel];
				activeProtocols.put(p.getPort(), p);
			}
			Captor.setActiveProtocols(activeProtocols);
			PacketSamurai.saveSnifferActiveProtocols();
		}
	}
}
