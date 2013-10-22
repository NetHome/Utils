/**
 * Copyright (C) 2005-2013, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome.
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
import java.util.List;

/**
 * Generic message unencoded for simple IR/RF protocols. A message consists of a number of fields with values.
 * This is used as container for decoded messages or messages intended for encoding. The available fields
 * depends on which protocol is used and are determined by the encoders/decoders.
 */
public interface Message extends Serializable {
    List<FieldValue> getFields();
}
