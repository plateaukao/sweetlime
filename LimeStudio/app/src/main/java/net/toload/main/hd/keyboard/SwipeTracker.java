/*
 *
 *  *
 *  **    Copyright 2015, The LimeIME Open Source Project
 *  **
 *  **    Project Url: http://github.com/lime-ime/limeime/
 *  **                 http://android.toload.net/
 *  **
 *  **    This program is free software: you can redistribute it and/or modify
 *  **    it under the terms of the GNU General Public License as published by
 *  **    the Free Software Foundation, either version 3 of the License, or
 *  **    (at your option) any later version.
 *  *
 *  **    This program is distributed in the hope that it will be useful,
 *  **    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  **    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  **    GNU General Public License for more details.
 *  *
 *  **    You should have received a copy of the GNU General Public License
 *  **    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  *
 *
 */

package net.toload.main.hd.keyboard;

import android.view.MotionEvent;

public class SwipeTracker {
    private static final int NUM_PAST = 4;
    private static final int LONGEST_PAST_TIME = 200;

    final EventRingBuffer mBuffer = new EventRingBuffer(NUM_PAST);

    private float mYVelocity;
    private float mXVelocity;

    public void addMovement(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mBuffer.clear();
            return;
        }
        long time = ev.getEventTime();
        final int count = ev.getHistorySize();
        for (int i = 0; i < count; i++) {
            addPoint(ev.getHistoricalX(i), ev.getHistoricalY(i), ev.getHistoricalEventTime(i));
        }
        addPoint(ev.getX(), ev.getY(), time);
    }

    private void addPoint(float x, float y, long time) {
        final EventRingBuffer buffer = mBuffer;
        while (buffer.size() > 0) {
            long lastT = buffer.getTime(0);
            if (lastT >= time - LONGEST_PAST_TIME)
                break;
            buffer.dropOldest();
        }
        buffer.add(x, y, time);
    }

    public void computeCurrentVelocity(int units) {
        computeCurrentVelocity(units, Float.MAX_VALUE);
    }

    public void computeCurrentVelocity(int units, float maxVelocity) {
        final EventRingBuffer buffer = mBuffer;
        final float oldestX = buffer.getX(0);
        final float oldestY = buffer.getY(0);
        final long oldestTime = buffer.getTime(0);

        float accumX = 0;
        float accumY = 0;
        final int count = buffer.size();
        for (int pos = 1; pos < count; pos++) {
            final int dur = (int)(buffer.getTime(pos) - oldestTime);
            if (dur == 0) continue;
            float dist = buffer.getX(pos) - oldestX;
            float vel = (dist / dur) * units;   // pixels/frame.
            if (accumX == 0) accumX = vel;
            else accumX = (accumX + vel) * .5f;

            dist = buffer.getY(pos) - oldestY;
            vel = (dist / dur) * units;   // pixels/frame.
            if (accumY == 0) accumY = vel;
            else accumY = (accumY + vel) * .5f;
        }
        mXVelocity = accumX < 0.0f ? Math.max(accumX, -maxVelocity)
                : Math.min(accumX, maxVelocity);
        mYVelocity = accumY < 0.0f ? Math.max(accumY, -maxVelocity)
                : Math.min(accumY, maxVelocity);
    }

    public float getXVelocity() {
        return mXVelocity;
    }

    public float getYVelocity() {
        return mYVelocity;
    }

    public static class EventRingBuffer {
        private final int bufSize;
        private final float xBuf[];
        private final float yBuf[];
        private final long timeBuf[];
        private int top;  // points new event
        private int end;  // points oldest event
        private int count; // the number of valid data

        public EventRingBuffer(int max) {
            this.bufSize = max;
            xBuf = new float[max];
            yBuf = new float[max];
            timeBuf = new long[max];
            clear();
        }

        public void clear() {
            top = end = count = 0;
        }

        public int size() {
            return count;
        }

        // Position 0 points oldest event
        private int index(int pos) {
            return (end + pos) % bufSize;
        }

        private int advance(int index) {
            return (index + 1) % bufSize;
        }

        public void add(float x, float y, long time) {
            xBuf[top] = x;
            yBuf[top] = y;
            timeBuf[top] = time;
            top = advance(top);
            if (count < bufSize) {
                count++;
            } else {
                end = advance(end);
            }
        }

        public float getX(int pos) {
            return xBuf[index(pos)];
        }

        public float getY(int pos) {
            return yBuf[index(pos)];
        }

        public long getTime(int pos) {
            return timeBuf[index(pos)];
        }

        public void dropOldest() {
            count--;
            end = advance(end);
        }
    }
}