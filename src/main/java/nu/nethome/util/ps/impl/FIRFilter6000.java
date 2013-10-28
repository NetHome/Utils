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

package nu.nethome.util.ps.impl;

/**
 * WinFilter version 0.8
 * http://www.winfilter.20m.com
 * akundert@hotmail.com

 * Filter type: Low Pass
 * Filter model: Rectangular Window
 * Sampling Frequency: 44 KHz
 * Cut Frequency: 6.000000 KHz
 * Coefficents Quantization: 16-bit
 */
public class FIRFilter6000 implements ProtocolSampler {
	static final int Ntap = 20;
	static final int DCgain = 65536;
	final int FIRCoef[] = { 
	
        2182,
        1189,
       -1005,
       -3140,
       -3586,
       -1227,
        3810,
       10027,
       15137,
       17110,
       15137,
       10027,
        3810,
       -1227,
       -3586,
       -3140,
       -1005,
        1189,
        2182,
        1650
	};


	private int x[] = new int[Ntap]; //input samples
	private boolean m_IsActive = true;
	private ProtocolSampler m_Output;
	
	public FIRFilter6000(ProtocolSampler output) {
		m_Output = output;
	}
	
	int filter(int NewSample) {
		int y=0;  //output sample
		int n;
		
		//shift the old samples
		for(n=Ntap-1; n>0; n--)
		   x[n] = x[n-1];
		
		//Calculate the new output
		x[0] = NewSample;
		for(n=0; n<Ntap; n++)
		    y += FIRCoef[n] * x[n];
		
		return y / DCgain;
	}

	public void addSample(int sample) {
		m_Output.addSample(m_IsActive ? filter(sample) : sample);
	}

	public void setSampleRate(int frequency) {
		m_Output.setSampleRate(frequency);
	}

	public boolean isActive() {
		return m_IsActive;
	}

	public void setActive(boolean isActive) {
		m_IsActive = isActive;
	}
}