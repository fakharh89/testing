package com.idevicesinc.sweetblue;

/**
 * 
 */
enum PE_TaskPriority
{
	TRIVIAL,	// for now only for scanning.
	LOW,		// for normal reads and writes and such.
	MEDIUM,		// default level for connection and bonding related tasks.
	HIGH,		// for implicit bonding and connection events, for example if user did something through another app or the OS, or came back into range.
	CRITICAL;	// for enabling/disabling ble and for removing bonds and disconnecting before actual ble disable.

	// Changed by MG from LOW to HIGH
	static final PE_TaskPriority FOR_NORMAL_READS_WRITES				= HIGH;//LOW;
	static final PE_TaskPriority FOR_EXPLICIT_BONDING_AND_CONNECTING	= MEDIUM;

	// Changed by MG from MEDIUM to HIGH
	static final PE_TaskPriority FOR_PRIORITY_READS_WRITES				= HIGH;//MEDIUM;
	static final PE_TaskPriority FOR_IMPLICIT_BONDING_AND_CONNECTING	= HIGH;
}
