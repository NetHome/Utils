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

/**
 * The ProtocolInfo holds general information about a specific protocol, such as
 * name of the protocol, manufacturer and so on. It is used to describe for example a
 * ProtocolDecoder.
 * @author Stefan
 *
 */
public class ProtocolInfo {

    private String name;
    private String type;
    private String company;
    private int length;
    private int defaultRepeatCount;

    /**
     * @param name Name of the protocol
     * @param type Type of encoding. May be for example: Mark Length or Space Length
     * @param company Manufacturer of devices using the protocol
     * @param length Length of a protocol message (in bits)
     * @param defaultRepeatCount number of repeats of signal by default
     */
    public ProtocolInfo(String name, String type, String company, int length, int defaultRepeatCount) {
        this.name = name;
        this.type = type;
        this.company = company;
        this.length = length;
        this.defaultRepeatCount = defaultRepeatCount;
    }

    /**
     * @return Name of the protocol
     */
    public String getName() {
        return name;
    }

    /**
     * @return Type of encoding. May be for example: Mark Length or Space Length
     */
    public String getType() {
        return type;
    }

    /**
     * @return Manufacturer of devices using the protocol
     */
    public String getCompany() {
        return company;
    }

    /**
     * @return Length of a protocol message (in bits)
     */
    public int getLength() {
        return length;
    }

    public int getDefaultRepeatCount() {
        return defaultRepeatCount;
    }
}
