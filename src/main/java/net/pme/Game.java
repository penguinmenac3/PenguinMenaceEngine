package net.pme;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JOptionPane;

import net.pme.objects.HudObject;
import net.pme.objects.MovableObject;
import net.pme.objects.Particle;
import net.pme.objects.Player;
import net.pme.objects.RenderableObject;

/**
 * Creates the LWJGL object and invokes a game loop thread as well as a network
 * listener etc.
 * 
 * @author Michael Fürst
 * @version 1.0
 */
public final class Game {
	private List<MovableObject> movableObjects = new CopyOnWriteArrayList<MovableObject>();
	private List<RenderableObject> renderableObjects = new CopyOnWriteArrayList<RenderableObject>();
	private List<HudObject> hudObjects = new CopyOnWriteArrayList<HudObject>();
	private LinkedList<Particle> particleObjects = new LinkedList<Particle>();
	private boolean isLoaded = false;
	private GameLoop gameLoop = null;

	/**
	 * Add a movable object to the movable objects.
	 * 
	 * Only works for objects that are not in renderable objects.
	 * 
	 * @param movableObject
	 *            The object that should be added.
	 */
	public final void addMovable(MovableObject movableObject) {
		if (!renderableObjects.contains(movableObject)) {
			movableObjects.add(movableObject);
		}
	}

	/**
	 * Add a renderable object to the renderable objects.
	 * 
	 * Removes objects from movable list, to reduce redundancy.
	 * 
	 * @param renderableObject
	 *            The object that should be added.
	 */
	public final void addRenderable(RenderableObject renderableObject) {
		if (movableObjects.contains(renderableObject)) {
			removeMovable(renderableObject);
		}
		renderableObjects.add(renderableObject);
	}

	/**
	 * Adds the specified hud object to the HUD.
	 * 
	 * @param hudObject
	 *            The specified hud object.
	 */
	public final void addHud(HudObject hudObject) {
		hudObjects.add(hudObject);
	}

	/**
	 * Remove an object from movable objects list.
	 * 
	 * @param movableObject
	 *            The object that should be removed.
	 */
	public final void removeMovable(MovableObject movableObject) {
		movableObjects.remove(movableObject);
	}

	/**
	 * Remove an object from the renderable objects list.
	 * 
	 * @param renderableObject
	 *            The object that should be removed.
	 */
	public final void removeRenderable(RenderableObject renderableObject) {
		renderableObjects.remove(renderableObject);
	}

	/**
	 * Remove an object from the hud object list.
	 * 
	 * @param hudObject
	 *            The object that should be removed.
	 */
	public final void removeHud(HudObject hudObject) {
		hudObjects.remove(hudObject);
	}

	/**
	 * Clear the movable object list.
	 */
	public final void clearMovable() {
		movableObjects.clear();
	}

	/**
	 * Clear the renderable object list.
	 */
	public final void clearRenderable() {
		renderableObjects.clear();
	}

	/**
	 * Clear the hud object list.
	 */
	public final void clearHud() {
		hudObjects.clear();
	}

	/**
	 * Clear the renderable, movable and hud object list.
	 */
	public final void clearAll() {
		clearMovable();
		clearRenderable();
		clearHud();
	}

	/**
	 * Run the game with the given player.
	 * 
	 * @param player
	 *            The player instance. (Null creates a dummy-player placed at 0)
	 */
	public final void runGame(Player player) {
		if (!isLoaded) {
			loadGame();
		}
		if (gameLoop != null) {
			throw new IllegalStateException(
					"There cannot be 2 calls of run game at a time");
		}

		gameLoop = new GameLoop();

		// Start the gameLoop
		gameLoop.run(movableObjects, renderableObjects, particleObjects,
				hudObjects, player);

		gameLoop = null;
	}

	/**
	 * Stop the currently running gameloop.
	 */
	public final void stopGame() {
		if (gameLoop == null) {
			throw new IllegalStateException("You must call runGame first.");
		}
		gameLoop.terminate();
	}

	/**
	 * Loads the game.
	 */
	public final void loadGame() {
		// TODO load settings.

		try {
			LibraryLoader.loadLibraries(this.getClass().getResource("/natives").getFile());
			String extraGameLibs = GameSettings.get().getLibraryPath();
			if (extraGameLibs != null && !extraGameLibs.equals("")) {
				LibraryLoader.loadLibraries(extraGameLibs);
			}
		} catch (Exception e) {
			JOptionPane.showConfirmDialog(null,
					"Cannot find/load native libraries!", "ERROR",
					JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}

		GameInput.load();

		// TODO invoke network listener thread.

		isLoaded = true;
	}
}