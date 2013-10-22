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

package nu.nethome.util.ps.impl;

import javax.sound.sampled.*;
import javax.sound.sampled.Mixer.Info;
import java.util.ArrayList;
import java.util.logging.Logger;


/**
 * The AudioPulsePlayer can take a pulse length sequence and "play" those pulses
 * on an audio channel. It is used in together with a hardware device which can
 * convert the audio signal to IR or RF signals and thereby send the signal to
 * control external devices. This class is used together with protocol encoders
 * which can create specific protocol messages for different type of devices.
 *
 * @author Stefan
 */
public class AudioPulsePlayer {

    private static final int CLOSE_LOOP_WAIT = 100;
    private static final int CLOSE_WAIT_LOOP_COUNT = 30;
    private static Logger logger = Logger.getLogger(AudioPulsePlayer.class.getName());

    private Position m_CurPosition = Position.NORMAL;
    private final int BUFFER_SIZE = 200000;
    private final int CHANNELS = 2;
    protected float m_SampleRate = 38000.0F;
    SourceDataLine m_AudioLine = null;
    int m_CurrentPos = 0;
    protected int m_CurrentSwing = 50;
    protected int m_CurrentSign = 1;


    protected byte m_DataBuffer[] = new byte[BUFFER_SIZE];
    private Info[] m_Mixers;
    private int m_SourceNumber = 0;
    private boolean m_IsOpen = false;

    enum Position {
        LEFT, RIGHT, NORMAL
    }

    ;

    public AudioPulsePlayer() {
        // Specify the audio format
        AudioFormat audioFormat = new AudioFormat(
                m_SampleRate,
                8,
                CHANNELS,    // Channels
                true,
                false);
        // Create a corresponding DataLine.Info
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

        ArrayList<Mixer.Info> resultMixers = new ArrayList<Mixer.Info>();
        Line.Info[] sources;

        // Get info on all available mixers
        Mixer.Info mixers[] = AudioSystem.getMixerInfo();
        for (int i = 0; i < mixers.length; i++) {
            // Find which mixers can supply at least one target data line of requester sort
            sources = AudioSystem.getMixer(mixers[i]).getSourceLineInfo(info);
            if (sources.length > 0) {
                resultMixers.add(mixers[i]);
            }
        }
        m_Mixers = resultMixers.toArray(new Mixer.Info[resultMixers.size()]);

    }

    /**
     * Opens the line for playing data. This must be performed before messages can
     * be played. If parameters such as sample speed, channel or line are changed,
     * the line must be closed and opened again.
     *
     * @return true if the line was opened, false otherwise
     */
    public boolean openLine() {

        if (m_IsOpen) {
            return true;
        }

        // Specify the audio format
        AudioFormat format = new AudioFormat(
                m_SampleRate,
                8,
                CHANNELS,
                true,
                false);

        //

        // Sanity check on source number
        if (m_SourceNumber >= m_Mixers.length) m_SourceNumber = 0;

        // Try to get a SourceDataLine. It is used to write audio data
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        try {
            m_AudioLine = (SourceDataLine) AudioSystem.getMixer(m_Mixers[m_SourceNumber]).getLine(info);
            m_AudioLine.open(format);
        } catch (LineUnavailableException e) {
            logger.warning("unable to get a playback line:" + e.getMessage());
            return false;
        }

        // NYI @ToDo FloatControl.Type.VOLUME;
        logger.finer("Player controls:");
        Control controls[] = m_AudioLine.getControls();
        for (Control control : controls) {
            logger.finer(control.toString());
        }
        if (m_AudioLine.isControlSupported(FloatControl.Type.PAN)) {
            FloatControl pan = (FloatControl) m_AudioLine
                    .getControl(FloatControl.Type.PAN);
            if (m_CurPosition == Position.RIGHT)
                pan.setValue(1.0f);
            else if (m_CurPosition == Position.LEFT)
                pan.setValue(-1.0f);
        }
        m_IsOpen = true;
        return true;
    }

    /**
     * Close the data line
     */
    public void closeLine() {
        if (m_AudioLine != null && m_IsOpen) {
            m_AudioLine.close();

            // Wait until the line is really closed. Sort of busy wait, a bit ugly
            int count = 0;
            do {
                try {
                    Thread.sleep(CLOSE_LOOP_WAIT);
                } catch (InterruptedException e) {
                    // Do Dinada
                }
                count++;
            } while (m_AudioLine.isOpen() && (count < CLOSE_WAIT_LOOP_COUNT));

            logger.info("Close wait count: " + Integer.toString(count));
            if (count == CLOSE_WAIT_LOOP_COUNT) {
                logger.warning("SourceDataLine Close failed");
            }

            m_AudioLine = null;
            m_IsOpen = false;
        }
    }

    @Deprecated
    public void old_stop() {
        if (m_AudioLine != null) {
            m_AudioLine.drain();
            m_AudioLine.close();
            m_AudioLine = null;
        }
    }

    @Deprecated
    public void play(byte buffer[], int length) {
        m_AudioLine.write(buffer, 0, length);
    }

    /**
     * @param startMessage an array of pulse lengths in micro seconds starting with the
     *                length of the first mark-pulse, followed by the length of the first space-pulse
     *                followed by the length of the second mark pulse and so on. This part of the message will be
     *                played once. The method will block until the entire message is played
     * @param repeatMessage this part of the message will be repeated as many times as specified in repeat
     * @param repeat number of times message is repeated
     * @return true if the massage was successfully played, false if an error occurred.
     */
    public boolean playMessage(int startMessage[], int repeatMessage[], int repeat) {
        if (!m_IsOpen) {
            return false;
        }
        m_CurrentPos = 0;

        // Since we use the full swing of the output signal with both negative
        // and positive values, we start with a ramp so the idle "0"-value of the line
        // goes down to our "0"-value which is -SWING.
        for (int p = 0; p < Math.abs(m_CurrentSwing); p += 2) {
            m_DataBuffer[m_CurrentPos++] = (byte) (-p * m_CurrentSign);
            m_DataBuffer[m_CurrentPos++] = (byte) (-p * m_CurrentSign);
        }

        // Encode the data pulses
        boolean mark = true;
        for (int pulse : startMessage) {
            encodePulse(pulse, mark);
            mark = !mark;
        }
        for (int i = 0; i < repeat; i++) {
            for (int pulse : repeatMessage) {
                encodePulse(pulse, mark);
                mark = !mark;
            }
        }

        // Check how much buffer space is available in the line
        int avaliable = m_AudioLine.available();

        // Write out as much data as possible without blocking before we actually
        // start the data line so we don't risk starving the player before we have
        // written any data
        int prewrite = (m_CurrentPos > avaliable) ? avaliable : m_CurrentPos;
        m_AudioLine.write(m_DataBuffer, 0, prewrite);

        // Now start the line playing data
        m_AudioLine.start();

        // If we have any data left to write, now write that too
        if (prewrite < m_CurrentPos) {
            // Write the rest of the pulse data
            m_AudioLine.write(m_DataBuffer, prewrite, m_CurrentPos - prewrite);
        }

        // Wait for all data to be written and then stop the line again, otherwise
        // it will starve and risk start sending garbage.
        m_AudioLine.drain();
        m_AudioLine.stop();

        return true;
    }

    public boolean playMessage(int message[]) {
        return playMessage(new int[0], message, 1);
    }

    /**
     * Encode a pulse with the current swing settings into the data buffer
     *
     * @param length_us Length of the pulse in micro seconds
     * @param mark      true if it is the mark flank, false if it is the space flank
     */
    protected void encodePulse(int length_us, boolean mark) {
        int intLength = (int) (m_SampleRate * length_us / 1000000.0);
        for (int i = 0; i < intLength; i++) {
            m_DataBuffer[m_CurrentPos++] = (byte) (mark ? m_CurrentSwing : -m_CurrentSwing);
            m_DataBuffer[m_CurrentPos++] = (byte) (mark ? m_CurrentSwing : -m_CurrentSwing);
        }
    }

    public void setSource(int sourceNumber) {
        if (sourceNumber < m_Mixers.length) {
            m_SourceNumber = sourceNumber;
        }
    }

    public int getSource() {
        return m_SourceNumber;
    }

    public Mixer.Info[] getSourceList() {
        return m_Mixers;
    }

    public float getSampleRate() {
        return m_SampleRate;
    }

    public void setSampleRate(float sampleRate) {
        m_SampleRate = sampleRate;
    }

    public boolean isOpen() {
        return m_IsOpen;
    }

    public void setSwing(int swing) {
        if ((swing > 127) || (swing < -128)) {
            return;
        }
        m_CurrentSwing = swing;
        m_CurrentSign = swing < 0 ? -1 : 1;
    }

    public int getSwing() {
        return m_CurrentSwing;
    }

    /**
     * This is only intended for module test purposes. It returns a copy of the
     * internal data buffer which is used to play audio data.
     *
     * @return internal data buffer
     */
    public byte[] getDataBuffer() {
        byte result[] = new byte[m_CurrentPos];
        for (int i = 0; i < m_CurrentPos; i++) {
            result[i] = m_DataBuffer[i];
        }
        return result;
    }
}
