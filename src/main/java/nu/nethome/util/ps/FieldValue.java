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

import java.io.Serializable;

/**
 * Helper class to store a field of data in a protocol message
 */
public class FieldValue implements Serializable {
    static final long serialVersionUID = 0;

    /**
     * Create an integer field
     *
     * @param name
     * @param value
     */
    public FieldValue(String name, int value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Create a string field
     *
     * @param name
     * @param value
     */
    public FieldValue(String name, String value) {
        this.name = name;
        this.value = -1;
        stringValue = value;
    }

    private String name = "";
    private int value = 0;
    private String stringValue = null;

    /**
     * @return true is the filed is a string field
     */
    public boolean isStringValue() {
        return stringValue != null;
    }

    public String getName() {
        return name;
    }

    /**
     * Get the integer value of the field. If this is a string filed the integer value is
     * a numeric interpretation of the string or -1 if the string is not numeric
     *
     * @return field value or -1
     */
    public int getValue() {
        if (isStringValue()) {
            int result;
            try {
                result = Integer.parseInt(stringValue);
            } catch (NumberFormatException e) {
                result = -1;
            }
            return result;
        }
        return value;
    }

    /**
     * Get the string value of the field. If this is an integer field, a string representation of the
     * integer value is returned.
     *
     * @return field value as string
     */
    public String getStringValue() {
        return isStringValue() ? stringValue : Integer.toString(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FieldValue that = (FieldValue) o;

        if (value != that.value) return false;
        if (!name.equals(that.name)) return false;
        if (stringValue != null ? !stringValue.equals(that.stringValue) : that.stringValue != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + value;
        result = 31 * result + (stringValue != null ? stringValue.hashCode() : 0);
        return result;
    }
}
