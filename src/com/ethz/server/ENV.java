package com.ethz.server;

import static net.fec.openrq.parameters.ParameterChecker.maxAllowedDataLength;

public class ENV {
	
	public static final String VERSION_NO = "1.00";

	// Fixed value for the payload length
	public static final int PAY_LEN = 1500 - 20 - 8; // UDP-Ipv4 payload length

    // Fixed value for the maximum decoding block size
	public static final int MAX_DEC_MEM = 8 * 1024 * 1024; // 8 MiB

    // The maximum allowed data length, given the parameters above
	public static final long MAX_DATA_LEN = maxAllowedDataLength(PAY_LEN, MAX_DEC_MEM);

}
