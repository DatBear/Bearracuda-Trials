/*
 * Copyright (c) 2021, Zoinkwiz <https://github.com/Zoinkwiz>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 * nucleon <https://github.com/nucleon>
 * Modification: drawLinesOnWorld()
 * Interpolates long lines into shorter segments to bypass RuneLite’s overlay draw distance limit.
 */

package com.datbear.overlay;

import java.util.ArrayList;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.List;

public class WorldLines {
    public static void createWorldMapLines(Graphics2D graphics, Client client, List<WorldPoint> linePoints,
            Color color) {
        Rectangle mapViewArea = WorldPerspective.getWorldMapClipArea(client);

        for (int i = 0; i < linePoints.size() - 1; i++) {
            Point startPoint = WorldPerspective.mapWorldPointToGraphicsPoint(client, linePoints.get(i));
            Point endPoint = WorldPerspective.mapWorldPointToGraphicsPoint(client, linePoints.get(i + 1));

            WorldLines.renderWorldMapLine(graphics, client, mapViewArea, startPoint, endPoint, color);
        }
    }

    public static void renderWorldMapLine(Graphics2D graphics, Client client, Rectangle mapViewArea, Point startPoint,
            Point endPoint, Color color) {
        if (mapViewArea == null || startPoint == null || endPoint == null) {
            return;
        }
        if (!mapViewArea.contains(startPoint.getX(), startPoint.getY())
                && !mapViewArea.contains(endPoint.getX(), endPoint.getY())) {
            return;
        }

        Line2D.Double line = new Line2D.Double(startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint.getY());
        DirectionArrow.drawLine(graphics, line, color, WorldPerspective.getWorldMapClipArea(client));
    }

    public static void drawLinesOnWorld(Graphics2D graphics, Client client, List<WorldPoint> linePoints, Color color,
            int z) {
        if (linePoints == null || linePoints.size() < 2) {
            return;
        }

        for (int i = 0; i < linePoints.size() - 1; i++) {
            WorldPoint startWp = linePoints.get(i);
            WorldPoint endWp = linePoints.get(i + 1);

            if (startWp == null || endWp == null)
                continue;
            if (startWp.equals(new WorldPoint(0, 0, 0)))
                continue;
            if (endWp.equals(new WorldPoint(0, 0, 0)))
                continue;
            if (startWp.getPlane() != endWp.getPlane())
                continue;

            List<WorldPoint> interpolated = interpolateLine(startWp, endWp);
            if (interpolated.isEmpty())
                continue;

            for (int j = 0; j < interpolated.size() - 1; j++) {
                WorldPoint wp1 = interpolated.get(j);
                WorldPoint wp2 = interpolated.get(j + 1);

                List<Point> points1 = WorldPerspective.worldToCanvasWithOffset(client, wp1, z);
                List<Point> points2 = WorldPerspective.worldToCanvasWithOffset(client, wp2, z);

                if (points1.isEmpty() || points2.isEmpty()) {
                    continue;
                }

                Point p1 = points1.get(0);
                Point p2 = points2.get(0);

                if (p1 == null || p2 == null) {
                    continue;
                }

                graphics.setColor(color);
                graphics.setStroke(new BasicStroke(2f));
                graphics.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            }
        }
    }

    private static Color dimColor(Color color, float factor) {
        factor = Math.min(Math.max(factor, 0f), 1f);
        int r = (int) (color.getRed() * factor);
        int g = (int) (color.getGreen() * factor);
        int b = (int) (color.getBlue() * factor);
        return new Color(r, g, b, color.getAlpha());
    }

    private static List<WorldPoint> interpolateLine(WorldPoint start, WorldPoint end) {
        List<WorldPoint> result = new ArrayList<>();
        int steps = Math.max(start.distanceTo(end), 1);

        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            int x = (int) Math.round(lerp(start.getX(), end.getX(), t));
            int y = (int) Math.round(lerp(start.getY(), end.getY(), t));
            int plane = start.getPlane();
            result.add(new WorldPoint(x, y, plane));
        }

        return result;
    }

    private static double lerp(int a, int b, double t) {
        return a + (b - a) * t;
    }
}
