package com.datbear;

import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.*;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import javax.inject.Inject;

import com.datbear.overlay.WorldLines;
import com.datbear.overlay.WorldPerspective;

import java.awt.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;

public class BearracudaTrialsOverlay extends Overlay {
    private static final Color GREEN = new Color(0, 255, 0, 150);
    private static final Color RED = new Color(255, 0, 0, 150);

    @Inject
    private ItemManager itemManager;

    @Inject
    private ModelOutlineRenderer modelOutlineRenderer;

    private Client client;
    private BearracudaTrialsPlugin plugin;
    private BearracudaTrialsConfig config;

    @Inject
    public BearracudaTrialsOverlay(Client client, BearracudaTrialsPlugin plugin, BearracudaTrialsConfig config) {
        super();
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.UNDER_WIDGETS);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        // // Render routes depending on configuration: Swordfish, Marlin, or Both
        // var mode = config.routeToShow();
        // if (mode == BearracudaTrialsConfig.RouteDisplay.Both || mode ==
        // BearracudaTrialsConfig.RouteDisplay.Swordfish) {
        // renderSwordfishLine(graphics);
        // }
        // if (mode == BearracudaTrialsConfig.RouteDisplay.Both || mode ==
        // BearracudaTrialsConfig.RouteDisplay.Marlin) {
        // renderMarlinLine(graphics);
        // }
        // if (config.showDebugOverlay()) {
        // renderDebugInfo(graphics);
        // }
        return null;
    }

    private void renderLine(Graphics2D graphics, java.util.List<WorldPoint> line) {
        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        if (line.size() < 2) {
            return;
        }

        WorldLines.drawLinesOnWorld(graphics, client, line, GREEN, line.get(0).getPlane());

        for (int i = 0; i < line.size(); i++) {
            var wp = line.get(i);
            var pts = WorldPerspective.worldToCanvasWithOffset(client, wp, wp.getPlane());
            if (pts.isEmpty())
                continue;
            var p = pts.get(0);

            renderLineDots(graphics, wp, GREEN, i, p);
        }
    }

    private void renderMarlinLine(Graphics2D graphics) {
        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        var player = client.getLocalPlayer();
        if (player == null)
            return;

        // Derive the player's instance WorldPoint by mapping their raw world
        // location (real-world) into the current instance. This keeps the
        // player's world view consistent with how we map route waypoints.
        final var playerLoc = WorldPerspective.getInstanceWorldPointFromReal(client, client.getTopLevelWorldView(),
                player.getWorldLocation());
        if (playerLoc == null)
            return;

        // draw only the next up-to-5 Marlin waypoints
        var visible = plugin.getVisibleMarlinLineForPlayer(playerLoc, 5);
        if (visible.size() >= 2) {
            WorldLines.drawLinesOnWorld(graphics, client, visible, GREEN, playerLoc.getPlane());
        }

        // Draw a single line from the player's boat location to the first
        // unvisited Marlin waypoint (if any). Use the top-level worldview so
        // the mapping uses plane 0.
        var boatLoc = BoatLocation.fromLocal(client, player.getLocalLocation());
        if (boatLoc != null) {
            var next = plugin.getNextUnvisitedMarlinWaypointIndices(boatLoc, 1);
            if (!next.isEmpty()) {
                var real = BearracudaTrialsPlugin.getTemporTantrumMarlinBestLine().get(next.get(0));
                var instp = WorldPerspective.getInstanceWorldPointFromReal(client, client.getTopLevelWorldView(), real);
                if (instp != null) {
                    java.util.List<WorldPoint> two = java.util.List.of(boatLoc, instp);
                    WorldLines.drawLinesOnWorld(graphics, client, two, Color.CYAN, boatLoc.getPlane());
                }
            }
        }

        var nextIndices = plugin.getNextUnvisitedMarlinWaypointIndices(playerLoc, 5);
        for (int idx : nextIndices) {
            var real = BearracudaTrialsPlugin.getTemporTantrumMarlinBestLine().get(idx);
            var wp = WorldPerspective.getInstanceWorldPointFromReal(client, client.getTopLevelWorldView(), real);
            if (wp == null)
                continue;
            var pts = WorldPerspective.worldToCanvasWithOffset(client, wp, wp.getPlane());
            if (pts.isEmpty())
                continue;
            var p = pts.get(0);

            renderLineDots(graphics, wp, GREEN, idx, p);
        }
    }

    private void renderSwordfishLine(Graphics2D graphics) {
        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        var player = client.getLocalPlayer();
        if (player == null)
            return;

        // Derive the player's instance WorldPoint by mapping from their raw
        // world location instead of using LocalPoint-derived worldpoints.
        final var playerLoc = WorldPerspective.getInstanceWorldPointFromReal(client, client.getTopLevelWorldView(),
                player.getWorldLocation());
        if (playerLoc == null)
            return;

        // Draw only the next up-to-5 unvisited waypoints (polyline from player -> next)
        var visible = plugin.getVisibleSwordfishLineForPlayer(playerLoc, 5);
        if (visible.size() >= 2) {
            WorldLines.drawLinesOnWorld(graphics, client, visible, GREEN, playerLoc.getPlane());
        }

        // Render markers/labels for the next unvisited targets (keep numbers equal to
        // the
        // original waypoints index so they match logs/debugging information)
        var nextIndices = plugin.getNextUnvisitedWaypointIndices(playerLoc, 5);
        for (int idx : nextIndices) {
            var real = BearracudaTrialsPlugin.getTemporTantrumSwordfishBestLine().get(idx);
            var wp = WorldPerspective.getInstanceWorldPointFromReal(client, client.getTopLevelWorldView(), real);
            if (wp == null)
                continue;
            var pts = WorldPerspective.worldToCanvasWithOffset(client, wp, wp.getPlane());
            if (pts.isEmpty())
                continue;
            var p = pts.get(0);

            renderLineDots(graphics, wp, GREEN, idx, p);
        }

        // Draw a single line from the player's boat location to the first
        // unvisited Swordfish waypoint (if any). Use top-level worldview so
        // the mapping uses plane 0.
        var boatLocSF = BoatLocation.fromLocal(client, player.getLocalLocation());
        if (boatLocSF != null) {
            var next = plugin.getNextUnvisitedWaypointIndices(boatLocSF, 1);
            if (!next.isEmpty()) {
                var real = BearracudaTrialsPlugin.getTemporTantrumSwordfishBestLine().get(next.get(0));
                var instp = WorldPerspective.getInstanceWorldPointFromReal(client, client.getTopLevelWorldView(), real);
                if (instp != null) {
                    java.util.List<WorldPoint> two = java.util.List.of(boatLocSF, instp);
                    WorldLines.drawLinesOnWorld(graphics, client, two, Color.CYAN, boatLocSF.getPlane());
                }
            }
        }
    }

    private void renderLineDots(Graphics2D graphics, WorldPoint wp, Color color, int i, Point start) {
        final int size = (i == 0 ? 10 : 6);
        final Color fill = (i == 0 ? new Color(0, 255, 255, 200) : new Color(255, 255, 255, 200));
        final Color border = new Color(0, 0, 0, 200);

        graphics.setColor(fill);
        graphics.fillOval(start.getX() - size / 2, start.getY() - size / 2, size, size);

        graphics.setColor(border);
        graphics.setStroke(new BasicStroke(2f));
        graphics.drawOval(start.getX() - size / 2, start.getY() - size / 2, size, size);

        // Draw label (index) near the point so it's easy to match route-to-data
        final String label = String.valueOf(i);
        graphics.setColor(Color.BLACK);
        graphics.setFont(graphics.getFont().deriveFont(Font.BOLD, 12f));
        graphics.drawString(label, start.getX() + (size / 2) + 2, start.getY() - (size / 2) - 2);
    }

    private void renderDebugInfo(Graphics2D graphics) {
        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        var player = client.getLocalPlayer();
        if (player == null)
            return;

        var boatLoc = BoatLocation.fromLocal(client, player.getLocalLocation());

        int x = 10;
        int y = 38;
        graphics.setFont(graphics.getFont().deriveFont(Font.BOLD, 12f));
        graphics.setColor(Color.WHITE);
        graphics.drawString("boat loc = " + (boatLoc == null ? "null" : boatLoc.toString()), x, y);
    }
}
