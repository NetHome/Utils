/**
 * Copyright (C) 2005-2013, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome (http://www.nethome.nu).
 *
 * OpenNetHome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenNetHome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nu.nethome.util.ps;

import java.util.ArrayList;
import java.util.List;

/**
 * The ProtocolMessage represents a decoded protocol message for simple pulse
 * messages of the type used by IR remote controls and simple RF based remote 
 * controls and thermometers. The ProtocolMessage adds extra information apart from the
 * field values. This class holds a bit too much information, but some of it is kept for
 * backward compatibility.
 *
 * @author Stefan
 */
public class ProtocolMessage implements Message {
	static final long serialVersionUID = 0;

    private String protocol = "";
	private int[] rawMessage = null;
	private List<FieldValue> fields;
	private String interpretation = "";
	private int command = 0;
	private int address = 0;
	private int repeat = 0;

    /**
     * Creates a protocol message.
     *
     * @param protocol Name of the protocol
     * @param command Integer representation of the protocol command
     * @param address Integer representation of the destination address/resource
     * @param rawLength Length of the raw binary message in bytes
     */
	public ProtocolMessage(String protocol, int command, int address, int rawLength) {
		this(protocol, command, address, new int[rawLength]);
	}

    /**
     * Creates a protocol message.
     *
     * @param protocol Name of the protocol
     * @param command Integer representation of the protocol command
     * @param address Integer representation of the destination address/resource
     * @param raw Binary message in bytes
     */
	public ProtocolMessage(String protocol, int command, int address, int[] raw) {
		this.protocol = protocol;
		this.command = command;
		this.address = address;
		rawMessage = raw;
		fields = new ArrayList<FieldValue>();
	}

	@Override
	public String toString() {
		return protocol + ": Command=" + Integer.toHexString(command) + 
		" Address=" +  Integer.toHexString(address);
	}

    /**
     * @return Name of the protocol this message belongs to
     */
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * @return The binary representation of the message.
     */
    public int[] getRawMessage() {
        return rawMessage;
    }

    /**
     * Sets a byte of the raw message at the specified index
     * @param index index to set at, starting with 0
     * @param value the byte value to set
     */
    public void setRawMessageByteAt(int index, int value) {
        this.rawMessage[index] = value;
    }

    /**
     * Add a new field to the message
     * @param field the field to add
     */
    public void addField(FieldValue field) {
        fields.add(field);
    }

    /**
     * Get all fields of the message
     * @return all fields
     */
    public List<FieldValue> getFields() {
        return fields;
    }

    /**
     * If the command/message has a name, this may be retrieved here.
     * @return name or an empty string
     */
    public String getInterpretation() {
        return interpretation;
    }

    public void setInterpretation(String interpretation) {
        this.interpretation = interpretation;
    }

    /**
     * @return Integer representation of the command this message represents
     */
    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    /**
     * @return Integer representation of the command address
     */
    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    /**
     * If this message is part of a repetition of a message, this is the number of repeated
     * messages received so far
     * @return number of times this message is repeated
     */
    public int getRepeat() {
        return repeat;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }
}
