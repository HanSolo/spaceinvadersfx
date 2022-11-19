package eu.hansolo.fx.spaceinvadersfx;

import eu.hansolo.toolboxfx.geom.Point;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


public class Main extends Application {
    private static final Random      RND                       = new Random();
    private static final double      WIDTH                     = 672;            // 224 Original width
    private static final double      HEIGHT                    = 768;            // 256 Original height
    private static final int         PIXEL_SIZE                = 3;
    private static final Rectangle2D VISUAL_BOUNDS             = new Rectangle2D(0, 0, WIDTH, HEIGHT);
    private static final boolean     IS_PORTRAIT_MODE          = VISUAL_BOUNDS.getHeight() > VISUAL_BOUNDS.getWidth();
    private static final double      BKG_SCALING_FACTOR        = IS_PORTRAIT_MODE ? (VISUAL_BOUNDS.getHeight() / HEIGHT) : (VISUAL_BOUNDS.getWidth() / WIDTH);
    private static final double      VELOCITY_FACTOR_Y         = BKG_SCALING_FACTOR;
    private static final double      TORPEDO_SPEED             = 6 * VELOCITY_FACTOR_Y;
    private static final double      INVADER_TORPEDO_SPEED     = 3 * VELOCITY_FACTOR_Y;
    private static final double      INVADER_WIDTH             = 36;
    private static final double      INVADER_HEIGHT            = 36;
    private static final double      INVADER_GRID_STEP_X       = 54;
    private static final double      INVADER_GRID_STEP_Y       = 54;
    private static final double      INITIAL_INVADERS_OFFSET_X = (WIDTH - (10 * INVADER_GRID_STEP_X)) * 0.5;
    private static final double      INITIAL_INVADERS_OFFSET_Y = 50;

    private static final int[][]     SHIELD = {
        { 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0 },
        { 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0 },
        { 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0 },
        { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 },
        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
        { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
        { 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1 },
        { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1 },
        { 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1 },
        { 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1 }
    };


    private AnimationTimer       timer;
    private long                 invaderInterval;
    private int                  invaderRowCounter;
    private long                 lastTimerCall;
    private long                 lastInvaderUpdateCall;
    private long                 lastHasBeenHitCall;
    private long                 lastMothershipCall;
    private long                 lastImpactCall;
    private long                 lastInvaderImpactCall;
    private boolean              state;
    private Canvas               canvas;
    private GraphicsContext      ctx;
    private List<Shield>         shields;
    private AudioClip            torpedoSnd;
    private AudioClip            explosionSnd;
    private AudioClip            shipExplosionSnd;
    private AudioClip            mothershipSnd;
    private List<AudioClip>      tones;
    private int                  toneCounter;
    private Image                invader1_1Img;
    private Image                invader1_2Img;
    private Image                invader2_1Img;
    private Image                invader2_2Img;
    private Image                invader3_1Img;
    private Image                invader3_2Img;
    private Image                mothershipImg;
    private Image                shipImg;
    private Image                shipExplosionImg;
    private Image                torpedoImg;
    private Image                torpedoGreenImg;
    private Image                torpedoRedImg;
    private Image                invaderTorpedoImg;
    private Image                invaderTorpedoGreenImg;
    private Image                invaderTorpedo2Img;
    private Image                invaderTorpedo2GreenImg;
    private Image                impactImg;
    private Image                invaderImpactImg;
    private Image                explosionImg;
    private Ship                 ship;
    private Map<Point, Image>    impacts;
    private Map<Point, Image>    invaderImpacts;
    private List<Mothership>     motherships;
    private List<Invader>        invaders;
    private List<Torpedo>        torpedoes;
    private List<InvaderTorpedo> invaderTorpedoes;
    private List<Explosion>      explosions;
    private double               invaderStepX;
    private int                  noOfLifes;
    private boolean              hasBeenHit;
    private long                 score;


    @Override public void init() {
        lastTimerCall         = System.nanoTime();
        lastInvaderUpdateCall = System.nanoTime();
        lastHasBeenHitCall    = System.nanoTime();
        lastMothershipCall    = System.nanoTime();

        state                 = true;
        invaderInterval       = 600_000_000l;
        invaderRowCounter     = 0;
        timer                 = new AnimationTimer() {
            @Override public void handle(final long now) {
                if (now > lastTimerCall) {
                    if (now > lastInvaderUpdateCall + invaderInterval) {
                        state ^= true;
                        updateInvaders();
                        playSound(tones.get(toneCounter));
                        toneCounter++;
                        if (toneCounter == 4) { toneCounter = 0; }
                        lastInvaderUpdateCall = now;
                    }
                    if (now > lastMothershipCall + 30_000_000_000l) {
                        motherships.add(new Mothership(mothershipImg, -mothershipImg.getWidth(), 30, RND.nextInt(100)));
                        playSound(mothershipSnd);
                        lastMothershipCall = now;
                    }
                    if (now > lastImpactCall + 250_000_000l) {
                        impacts.clear();
                    }
                    if (now > lastInvaderImpactCall + 250_000_000l) {
                        invaderImpacts.clear();
                    }
                    if (hasBeenHit && now > lastHasBeenHitCall + 2_000_000_000l) {
                        hasBeenHit = false;
                        ship.respawn();
                    }
                    updateAndDraw();
                    switch(invaderRowCounter) {
                        case 4  -> invaderInterval  = 500_000_000l;
                        case 8  -> invaderInterval  = 400_000_000l;
                        case 12 -> invaderInterval = 300_000_000;
                        case 16 -> invaderInterval = 200_000_000;
                        case 20 -> invaderInterval = 100_000_000;
                    }
                    lastTimerCall = now;
                }
            }
        };
        canvas  = new Canvas(WIDTH, HEIGHT);
        ctx     = canvas.getGraphicsContext2D();

        // Load all sounds
        toneCounter = 0;
        tones       = new ArrayList<>();
        tones.add(new AudioClip(getClass().getResource("tone1.mp3").toExternalForm()));
        tones.add(new AudioClip(getClass().getResource("tone2.mp3").toExternalForm()));
        tones.add(new AudioClip(getClass().getResource("tone3.mp3").toExternalForm()));
        tones.add(new AudioClip(getClass().getResource("tone4.mp3").toExternalForm()));
        loadSounds();

        // Load all images
        loadImages();

        // Initialize shields
        shields = new ArrayList<>();
        for (int i = 0 ; i < 4 ; i++) {
            shields.add(new Shield(54 + i * 170, HEIGHT - 150));
        }

        // Initialize invaders, mothership and ship
        ship = new Ship(shipImg);

        invaders         = new ArrayList<>();
        for (int iy = 0 ; iy < 5 ; iy++) {
            for (int ix = 0 ; ix < 11 ; ix++) {
                Invader invader = null;
                switch(iy) {
                    case 0   -> invader = new Invader(invader3_1Img, invader3_2Img, INITIAL_INVADERS_OFFSET_X + ix * INVADER_GRID_STEP_X, INITIAL_INVADERS_OFFSET_Y + iy * INVADER_GRID_STEP_Y, 30);
                    case 1,2 -> invader = new Invader(invader1_1Img, invader1_2Img, INITIAL_INVADERS_OFFSET_X + ix * INVADER_GRID_STEP_X, INITIAL_INVADERS_OFFSET_Y + iy * INVADER_GRID_STEP_Y, 20);
                    case 3,4 -> invader = new Invader(invader2_1Img, invader2_2Img, INITIAL_INVADERS_OFFSET_X + ix * INVADER_GRID_STEP_X, INITIAL_INVADERS_OFFSET_Y + iy * INVADER_GRID_STEP_Y, 10);
                }
                invaders.add(invader);
            }
        }
        impacts          = new HashMap<>();
        invaderImpacts   = new HashMap<>();
        motherships      = new ArrayList<>();
        torpedoes        = new ArrayList<>();
        invaderTorpedoes = new ArrayList<>();
        explosions       = new ArrayList<>();
        invaderStepX     = 9;
        noOfLifes        = 3;
        hasBeenHit       = false;
        score            = 0;
    }

    @Override public void start(final Stage stage) {
        StackPane pane = new StackPane(canvas);

        Scene scene = new Scene(pane, WIDTH, HEIGHT);

        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case RIGHT -> moveShipRight();
                case LEFT  -> moveShipLeft();
                case SPACE -> fire(ship.x, ship.y);
            }
        });
        scene.setOnKeyReleased(e -> {
            switch(e.getCode()) {
                case RIGHT -> stopShip();
                case LEFT  -> stopShip();
            }
        });

        stage.setTitle("SpaceInvadersFX");
        stage.setScene(scene);
        stage.show();
        stage.setResizable(false);

        timer.start();
    }

    @Override public void stop() {
        Platform.exit();
    }


    // Helper methods
    private void loadSounds() {
        torpedoSnd       = new AudioClip(getClass().getResource("torpedo.mp3").toExternalForm());
        explosionSnd     = new AudioClip(getClass().getResource("explosion.mp3").toExternalForm());
        shipExplosionSnd = new AudioClip(getClass().getResource("shipExplosion.mp3").toExternalForm());
        mothershipSnd    = new AudioClip(getClass().getResource("mothership.mp3").toExternalForm());
        mothershipSnd.setCycleCount(-1);
    }

    private void loadImages() {
        invader1_1Img           = new Image(getClass().getResourceAsStream("invader1_1.png"), 36, 36, true, false);
        invader1_2Img           = new Image(getClass().getResourceAsStream("invader1_2.png"), 36, 36, true, false);
        invader2_1Img           = new Image(getClass().getResourceAsStream("invader2_1.png"), 36, 36, true, false);
        invader2_2Img           = new Image(getClass().getResourceAsStream("invader2_2.png"), 36, 36, true, false);
        invader3_1Img           = new Image(getClass().getResourceAsStream("invader3_1.png"), 36, 36, true, false);
        invader3_2Img           = new Image(getClass().getResourceAsStream("invader3_2.png"), 36, 36, true, false);
        mothershipImg           = new Image(getClass().getResourceAsStream("mothership.png"), 48, 20, true, false);
        shipImg                 = new Image(getClass().getResourceAsStream("ship.png"), 45, 24, true, false);
        shipExplosionImg        = new Image(getClass().getResourceAsStream("shipexplosion.png"), 45, 24, true, false);
        torpedoImg              = new Image(getClass().getResourceAsStream("torpedo.png"), 3, 9, true, false);
        torpedoGreenImg         = new Image(getClass().getResourceAsStream("torpedo_green.png"), 3, 9, true, false);
        torpedoRedImg           = new Image(getClass().getResourceAsStream("torpedo_red.png"), 3, 9, true, false);
        explosionImg            = new Image(getClass().getResourceAsStream("explosion.png"), 39, 36, true, false);
        invaderTorpedoImg       = new Image(getClass().getResourceAsStream("invadertorpedo.png"), 9, 21, true, false);
        invaderTorpedoGreenImg  = new Image(getClass().getResourceAsStream("invadertorpedo_green.png"), 9, 21, true, false);
        invaderTorpedo2Img      = new Image(getClass().getResourceAsStream("invadertorpedo2.png"), 9, 21, true, false);
        invaderTorpedo2GreenImg = new Image(getClass().getResourceAsStream("invadertorpedo2_green.png"), 9, 21, true, false);
        impactImg               = new Image(getClass().getResourceAsStream("impact.png"), 21, 21, true, false);
        invaderImpactImg        = new Image(getClass().getResourceAsStream("invaderImpact.png"), 21, 21, true, false);
    }

    private static String padLeft(final String text, final String filler, final int n) {
        return String.format("%" + n + "s", text).replace(" ", filler);
    }

    private static boolean isBitSet(final int posX, final int posY, int[][] bitMatrix) {
        if (posX < 0 || posY < 0 || posX >= bitMatrix[0].length || posY >= bitMatrix.length) { return false; }
        return bitMatrix[posY][posX] != 0;
    }
    private static boolean isBitZero(final int posX, final int posY, int[][] bitMatrix) {
        if (posX < 0 || posY < 0 || posX > bitMatrix[0].length || posY > bitMatrix.length) { return false; }
        return bitMatrix[posY][posX] == 0;
    }

    private static void setBit(final int posX, final int posY, int[][] bitMatrix, final int bit) {
        if (posX < 0 || posY < 0 || posX >= bitMatrix[0].length || posY >= bitMatrix.length || bit < 0 || bit > 1) { return; }
        bitMatrix[posY][posX] = bit;
    }

    // Hit test
    private static boolean isHitCircleCircle(final double c1X, final double c1Y, final double c1R, final double c2X, final double c2Y, final double c2R) {
        double distX    = c1X - c2X;
        double distY    = c1Y - c2Y;
        double distance = Math.sqrt((distX * distX) + (distY * distY));
        return (distance <= c1R + c2R);
    }

    // Shield hit test
    private static boolean isHitShield(final double x, final double y, final Shield shield, final boolean fromTop) {
        if (x < shield.x || x > shield.x + 54) { return false; }
        if (y > shield.y + 45 || y < shield.y) { return false; }

        int posX = (int) ((x - shield.x) / 3);

        if (fromTop) { // Hit from invader torpedo
            if (isHitCircleCircle(x, y + 1.5, 3, shield.x + shield.width * 0.5, shield.y + shield.height * 0.5, shield.radius)) {
                for (int posY = 0; posY < 15; posY++) {
                    if (isBitSet(18 - posX, posY, shield.bitmatrix)) {
                        setBit(18 - posX, posY, shield.bitmatrix, 0);
                        setBit(18 - posX - 1, posY, shield.bitmatrix, 0);
                        setBit(18 - posX, posY + 1, shield.bitmatrix, 0);
                        setBit(18 - posX - 1, posY + 1, shield.bitmatrix, 0);
                        setBit(18 - posX + 1, posY + 1, shield.bitmatrix, 0);
                        setBit(18 - posX, posY + 2, shield.bitmatrix, 0);
                        setBit(18 - posX - 1, posY + 2, shield.bitmatrix, 0);
                        setBit(18 - posX + 1, posY + 2, shield.bitmatrix, 0);
                        setBit(18 - posX, posY + 3, shield.bitmatrix, 0);
                        setBit(18 - posX - 1, posY + 3, shield.bitmatrix, 0);
                        setBit(18 - posX - 2, posY + 3, shield.bitmatrix, 0);
                        setBit(18 - posX + 1, posY + 3, shield.bitmatrix, 0);
                        setBit(18 - posX + 2, posY + 3, shield.bitmatrix, 0);
                        setBit(18 - posX, posY + 4, shield.bitmatrix, 0);
                        setBit(18 - posX - 1, posY + 4, shield.bitmatrix, 0);
                        setBit(18 - posX - 2, posY + 4, shield.bitmatrix, 0);
                        setBit(18 - posX + 1, posY + 4, shield.bitmatrix, 0);
                        setBit(18 - posX + 2, posY + 4, shield.bitmatrix, 0);
                        setBit(18 - posX, posY + 5, shield.bitmatrix, 0);
                        setBit(18 - posX + 1, posY + 5, shield.bitmatrix, 0);
                        setBit(18 - posX - 1, posY + 5, shield.bitmatrix, 0);
                        setBit(18 - posX + 1, posY + 5, shield.bitmatrix, 0);

                        setBit(18 - posX, posY + 6, shield.bitmatrix, 0);
                        setBit(18 - posX - 2, posY + 6, shield.bitmatrix, 0);
                        setBit(18 - posX + 2, posY + 6, shield.bitmatrix, 0);
                        return true;
                    }
                }
            }
        } else { // Hit from own torpedo
            if (isHitCircleCircle(x, y + 1.5, 3, shield.x + shield.width * 0.5, shield.y + shield.height * 0.5, shield.radius)) {
                for (int posY = 14; posY >= 0; posY--) {
                    if (isBitSet(18 - posX, posY, shield.bitmatrix)) {
                        setBit(18 - posX, posY, shield.bitmatrix, 0);
                        setBit(18 - posX, posY - 1, shield.bitmatrix, 0);
                        setBit(18 - posX, posY - 2, shield.bitmatrix, 0);
                        return true;
                    }
                }
            }
        }
        return false;
    }


    // ******************** Game control **************************************
    private void moveShipRight() {
        ship.vX = 2;
    }

    private void moveShipLeft() {
        ship.vX = -2;
    }

    private void stopShip() { ship.vX = 0; }

    private void fire(final double x, final double y) {
        if (torpedoes.size() > 0) { return; }
        torpedoes.add(new Torpedo(torpedoImg, torpedoRedImg, torpedoGreenImg, x, y));
        playSound(torpedoSnd);
    }

    private void fireInvaderTorpedo(final double x, final double y) {
        if (invaderTorpedoes.size() > 0) { return; }
        int img = RND.nextInt(2);
        invaderTorpedoes.add(new InvaderTorpedo(img == 0 ? invaderTorpedoImg : invaderTorpedo2Img, img == 0 ? invaderTorpedoGreenImg : invaderTorpedo2GreenImg, x, y));
    }


    // Play audio clips
    private void playSound(final AudioClip audioClip) { audioClip.play(); }


    // Re-Init
    private void reinit() {
        lastTimerCall         = System.nanoTime();
        lastInvaderUpdateCall = System.nanoTime();
        lastHasBeenHitCall    = System.nanoTime();
        lastMothershipCall    = System.nanoTime();
        state                 = true;
        invaderInterval       = 600_000_000l;
        invaderRowCounter     = 0;

        // Initialize shields
        shields.clear();
        for (int i = 0 ; i < 4 ; i++) {
            shields.add(new Shield(54 + i * 170, HEIGHT - 150));
        }

        // Initialize invaders, mothership and ship
        ship = new Ship(shipImg);

        invaders.clear();
        for (int iy = 0 ; iy < 5 ; iy++) {
            for (int ix = 0 ; ix < 11 ; ix++) {
                Invader invader = null;
                switch(iy) {
                    case 0   -> invader = new Invader(invader3_1Img, invader3_2Img, INITIAL_INVADERS_OFFSET_X + ix * INVADER_GRID_STEP_X, INITIAL_INVADERS_OFFSET_Y + iy * INVADER_GRID_STEP_Y, 30);
                    case 1,2 -> invader = new Invader(invader1_1Img, invader1_2Img, INITIAL_INVADERS_OFFSET_X + ix * INVADER_GRID_STEP_X, INITIAL_INVADERS_OFFSET_Y + iy * INVADER_GRID_STEP_Y, 20);
                    case 3,4 -> invader = new Invader(invader2_1Img, invader2_2Img, INITIAL_INVADERS_OFFSET_X + ix * INVADER_GRID_STEP_X, INITIAL_INVADERS_OFFSET_Y + iy * INVADER_GRID_STEP_Y, 10);
                }
                invaders.add(invader);
            }
        }
        motherships.clear();
        torpedoes.clear();
        invaderTorpedoes.clear();
        explosions.clear();
        noOfLifes        = 3;
        hasBeenHit       = false;
    }


    // Game Over
    private void gameOver() {
        System.exit(0);
    }


    // ******************** Redraw ********************************************
    private void updateInvaders() {
        if (invaders.isEmpty()) { reinit(); }

        double maxX = invaders.parallelStream().max(Comparator.comparingDouble(Invader::getX)).get().x + INVADER_WIDTH;
        double minX = invaders.parallelStream().min(Comparator.comparingDouble(Invader::getX)).get().x - INVADER_HEIGHT;

        if (maxX > WIDTH && invaderStepX > 0) {
            invaderStepX = -9;
            invaders.forEach(invader -> invader.y += 9);
            invaderRowCounter++;
            return;
        } else if (minX < 0 && invaderStepX < 0) {
            invaderStepX = 9;
            invaders.forEach(invader -> invader.y += 9);
            invaderRowCounter++;
            return;
        }

        invaders.forEach(invader -> invader.x += invaderStepX);

        // Update random invader
        invaders.get(RND.nextInt(invaders.size())).update();

        // Remove explosions
        explosions.removeIf(sprite -> sprite.toBeRemoved);
    }

    private void updateAndDraw() {
        ctx.clearRect(0, 0, WIDTH, HEIGHT);
        ctx.setFill(Color.BLACK);
        ctx.fillRect(0, 0, WIDTH, HEIGHT);


        // Shields
        ctx.setFill(Color.LIME);
        ctx.setStroke(Color.LIME);
        shields.forEach(shield -> {
            for (int px = 0 ; px < 18 ; px++) {
                for (int py = 0 ; py < 15 ; py++) {
                    if (isBitZero(17 -px, py, shield.bitmatrix)) continue;
                    ctx.fillRect(shield.x + px * PIXEL_SIZE, shield.y + py + py * PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE);
                    ctx.strokeRect(shield.x + px * PIXEL_SIZE, shield.y + py + py * PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE);
                }
            }
        });


        // Draw Torpedos
        for (Torpedo torpedo : torpedoes) {
            torpedo.update();
            ctx.drawImage(torpedo.y > HEIGHT - 150 ? torpedo.image3 : torpedo.y < 60 ? torpedo.image2 : torpedo.image, torpedo.x - torpedo.radius, torpedo.y - torpedo.radius);
            shields.forEach(shield -> {
                if (isHitShield(torpedo.x + torpedo.width * 0.5, torpedo.y, shield, false)) { torpedo.toBeRemoved = true; }
            });
        }


        // Draw mothership
        motherships.forEach(mothership -> {
            mothership.update();

            // Draw mothership
            ctx.drawImage(mothershipImg, mothership.x - mothership.radius, mothership.y - mothership.radius);

            // Check for torpedo hits
            for (Torpedo torpedo : torpedoes) {
                if (isHitCircleCircle(torpedo.x, torpedo.y, torpedo.radius, mothership.x, mothership.y, mothership.radius)) {
                    Explosion explosion = new Explosion(explosionImg, mothership.x, mothership.y + mothership.height);
                    explosions.add(explosion);
                    score += mothership.value;
                    mothership.toBeRemoved = true;
                    torpedo.toBeRemoved = true;

                    //playSound(spaceShipExplosionSound);
                }
            }
        });


        // Draw invaders
        invaders.forEach(invader -> {
            // Draw invader
            ctx.drawImage(state ? invader.image : invader.image2, invader.x - invader.radius, invader.y - invader.radius);

            // Check for torpedo hits
            for (Torpedo torpedo : torpedoes) {
                if (isHitCircleCircle(torpedo.x, torpedo.y, torpedo.radius, invader.x, invader.y, invader.radius)) {
                    Explosion explosion = new Explosion(explosionImg, invader.x, invader.y + invader.height);
                    explosions.add(explosion);
                    score += invader.value;
                    invader.toBeRemoved = true;
                    torpedo.toBeRemoved = true;

                    playSound(explosionSnd);
                }
            }
        });


        // Draw Invadertorpedos
        for (InvaderTorpedo invaderTorpedo : invaderTorpedoes) {
            invaderTorpedo.update();
            ctx.drawImage(invaderTorpedo.y > HEIGHT - 150 ? invaderTorpedo.image2 : invaderTorpedo.image, invaderTorpedo.x - invaderTorpedo.radius, invaderTorpedo.y - invaderTorpedo.radius);
            shields.forEach(shield -> {
                if (isHitShield(invaderTorpedo.x + invaderTorpedo.width * 0.5, invaderTorpedo.y, shield, true)) { invaderTorpedo.toBeRemoved = true; }
            });
        }


        // Draw Explosions
        for (Explosion explosion : explosions) {
            explosion.update();
            ctx.drawImage(explosion.image, explosion.x - explosion.radius, explosion.y - explosion.radius);
            explosion.toBeRemoved = true;
        }


        // Draw impacts
        impacts.entrySet().forEach(entry -> ctx.drawImage(entry.getValue(), entry.getKey().getX(), entry.getKey().getY()));


        // Draw invader impacts
        invaderImpacts.entrySet().forEach(entry -> ctx.drawImage(entry.getValue(), entry.getKey().getX(), entry.getKey().getY()));


        // Draw ship
        if (noOfLifes > 0) {
            if (hasBeenHit) {
                ctx.drawImage(shipExplosionImg, ship.x - ship.radius, ship.y - ship.radius);
            } else {
                ship.update();
                ctx.drawImage(shipImg,ship.x - ship.radius, ship.y - ship.radius);
            }
        }


        // Draw score
        ctx.setFill(Color.WHITE);
        ctx.setFont(Font.font(16));
        ctx.setTextAlign(TextAlignment.RIGHT);
        ctx.setTextBaseline(VPos.TOP);
        ctx.fillText(padLeft(Long.toString(score), "0", 4), 80, 10);


        // Draw no of lifes
        ctx.setTextAlign(TextAlignment.LEFT);
        ctx.fillText(Integer.toString(noOfLifes), 15, HEIGHT - 20);

        ctx.save();
        ctx.setStroke(Color.LIME);
        ctx.setLineWidth(2);
        ctx.strokeLine(0, HEIGHT - 25, WIDTH, HEIGHT - 25);
        ctx.restore();
        for (int i = 0 ; i < noOfLifes ; i++) {
            ctx.drawImage(shipImg, 40 + i * 54, HEIGHT - 25);
        }


        // Remove sprites
        torpedoes.removeIf(sprite -> sprite.toBeRemoved);
        invaderTorpedoes.removeIf(sprite -> sprite.toBeRemoved);
        invaders.removeIf(sprite -> sprite.toBeRemoved);
        motherships.removeIf(sprite -> sprite.toBeRemoved);
    }


    // ******************** Inner Classes *************************************
    private abstract class Sprite {
        public Image   image;
        public double  x;
        public double  y;
        public double  r;
        public double  vX;
        public double  vY;
        public double  vR;
        public double  width;
        public double  height;
        public double  size;
        public double  radius;
        public boolean toBeRemoved;


        public Sprite() {
            this(null, 0, 0, 0, 0, 0, 0);
        }
        public Sprite(final Image image) {
            this(image, 0, 0, 0, 0, 0, 0);
        }
        public Sprite(final Image image, final double x, final double y) {
            this(image, x, y, 0, 0, 0, 0);
        }
        public Sprite(final Image image, final double x, final double y, final double vX, final double vY) {
            this(image, x, y, 0, vX, vY, 0);
        }
        public Sprite(final Image image, final double x, final double y, final double r, final double vX, final double vY) {
            this(image, x, y, r, vX, vY, 0);
        }
        public Sprite(final Image image, final double x, final double y, final double r, final double vX, final double vY, final double vR) {
            this.image       = image;
            this.x           = x;
            this.y           = y;
            this.r           = r;
            this.vX          = vX;
            this.vY          = vY;
            this.vR          = vR;
            this.width       = null == image ? 0 : image.getWidth();
            this.height      = null == image ? 0 : image.getHeight();
            this.size        = this.width > this.height ? width : height;
            this.radius      = this.size * 0.5;
            this.toBeRemoved = false;
        }


        protected void init() {}

        public void respawn() {}

        public abstract void update();
    }

    public abstract class AnimatedSprite extends Sprite {
        protected final int    maxFrameX;
        protected final int    maxFrameY;
        protected       double scale;
        protected       int    countX;
        protected       int    countY;


        public AnimatedSprite(final int maxFrameX, final int maxFrameY, final double scale) {
            this(0, 0, 0, 0, 0, 0, maxFrameX, maxFrameY, scale);
        }
        public AnimatedSprite(final double x, final double y, final double vX, final double vY, final int maxFrameX, final int maxFrameY, final double scale) {
            this(x, y, 0, vX, vY, 0, maxFrameX, maxFrameY, scale);
        }
        public AnimatedSprite(final double x, final double y, final double r, final double vX, final double vY, final double vR, final int maxFrameX, final int maxFrameY, final double scale) {
            super(null, x, y, r, vX, vY, vR);
            this.maxFrameX = maxFrameX;
            this.maxFrameY = maxFrameY;
            this.scale     = scale;
            this.countX    = 0;
            this.countY    = 0;
        }


        @Override public void update() {
            x += vX;
            y += vY;

            countX++;
            if (countX == maxFrameX) {
                countY++;
                if (countX == maxFrameX && countY == maxFrameY) {
                    toBeRemoved = true;
                }
                countX = 0;
                if (countY == maxFrameY) {
                    countY = 0;
                }
            }
        }
    }

    private class Ship extends Sprite {

        public Ship(final Image image) {
            super(image);
            init();
        }


        @Override protected void init() {
            this.x      = WIDTH * 0.5;
            this.y      = HEIGHT - image.getHeight() - 20;
            this.width  = image.getWidth();
            this.height = image.getHeight();
            this.size   = width > height ? width : height;
            this.radius = size * 0.5;
            this.vX     = 0;
            this.vY     = 0;
        }

        @Override public void respawn() {
            this.x  = WIDTH * 0.5;
            this.vX = 0;
            this.vY = 0;
        }


        @Override public void update() {
            x += vX;
            y += vY;
            if (x + width * 0.5 > WIDTH) {
                x = WIDTH - width * 0.5;
            }
            if (x - width * 0.5 < 0) {
                x = width * 0.5;
            }
            if (y + height * 0.5 > HEIGHT) {
                y = HEIGHT - height * 0.5;
            }
            if (y - height * 0.5 < 0) {
                y = height * 0.5;
            }
        }
    }

    private class Invader extends Sprite {
        public static final  long    TIME_BETWEEN_SHOTS  = 500_000_000l;
        public               Image   image2;
        public               int     value;
        public               long    lastShot;
        public               boolean toBeRemoved;


        public Invader(final Image image, final Image image2, final double x, final double y, final int value) {
            super(image);
            this.image2      = image2;
            this.toBeRemoved = false;
            this.x           = x;
            this.y           = y;
            this.value       = value;
            init();
        }


        public double getX() { return x; }

        @Override protected void init() {
            width  = image.getWidth();
            height = image.getHeight();
            size   = width > height ? width : height;
            radius = size * 0.5;

            // Velocity
            vX = 0;
            vY = 0;

            lastShot = System.nanoTime();
        }

        @Override public void update() {
            if (toBeRemoved) { return; }
            //x += vX;
            //y += vY;

            long now = System.nanoTime();
            if (now - lastShot > TIME_BETWEEN_SHOTS) {
                fireInvaderTorpedo(x, y);
                lastShot = now;
            }
            if (y > HEIGHT - 150) {
                gameOver();
            }
        }
    }

    private class Mothership extends Sprite {
        public int     value;
        public boolean toBeRemoved;


        public Mothership(final Image image, final double x, final double y, final int value) {
            super(image);
            this.toBeRemoved = false;
            this.x           = x;
            this.y           = y;
            this.value       = value;
            init();
        }


        @Override protected void init() {
            width  = image.getWidth();
            height = image.getHeight();
            size   = width > height ? width : height;
            radius = size * 0.5;

            // Velocity
            vX = 2;
            vY = 0;
        }

        @Override public void update() {
            if (toBeRemoved) { return; }
            x += vX;
            y += vY;

            if (x >= WIDTH) {
                toBeRemoved = true;
                mothershipSnd.stop();
            }
        }
    }

    private class Torpedo extends Sprite {
        private Image image2;
        private Image image3;

        public Torpedo(final Image image, final Image image2, final Image image3, final double x, final double y) {
            super(image, x, y - image.getHeight(), 0, TORPEDO_SPEED);
            this.image2 = image2;
            this.image3 = image3;
        }

        @Override public void update() {
            y -= vY;
            if (y < -size) {
                toBeRemoved = true;
                impacts.put(new Point(x - impactImg.getWidth() * 0.5, y + impactImg.getHeight() * 0.5), impactImg);
                lastImpactCall = System.nanoTime();
            }
        }
    }

    private class Explosion extends Sprite {
        public Explosion(final Image image, final double x, final double y) {
            super(image, x, y - image.getHeight(), 0, 0);
        }

        @Override public void update() {
            y -= vY;
            if (y < -size) {
                toBeRemoved = true;
            }
        }
    }

    private class InvaderTorpedo extends Sprite {
        private Image image2;

        public InvaderTorpedo(final Image image, final Image image2, final double x, final double y) {
            super(image, x, y, 0, INVADER_TORPEDO_SPEED);
            this.image2 = image2;
        }

        @Override public void update() {
            y += vY;

            if (!hasBeenHit) {
                boolean hit = isHitCircleCircle(x, y, radius, ship.x, ship.y, ship.radius);
                if (hit) {
                    toBeRemoved        = true;
                    hasBeenHit         = true;
                    lastHasBeenHitCall = System.nanoTime();
                    playSound(shipExplosionSnd);
                    noOfLifes--;
                    if (0 == noOfLifes) {
                        gameOver();
                    }
                }
            }

            if (y + height > HEIGHT - 10) {
                toBeRemoved = true;
                invaderImpacts.put(new Point(x - invaderImpactImg.getWidth() * 0.5, y - invaderImpactImg.getHeight()), invaderImpactImg);
                lastInvaderImpactCall = System.nanoTime();
            }
        }
    }

    public class Shield {
        public int[][] bitmatrix = {
            { 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0 },
            { 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0 },
            { 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0 },
            { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 },
            { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
            { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
            { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
            { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
            { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
            { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
            { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
            { 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1 },
            { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1 },
            { 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1 },
            { 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1 }
        };
        public final double  width;
        public final double  height;
        public final double  x;
        public final double  y;
        public final double  radius;


        public Shield(final double x, final double y) {
            this.width  = 54;
            this.height = 45;
            this.x      = x;
            this.y      = y;
            this.radius = this.width * 0.5;
        }
    }


    // ******************** Start *********************************************
    public static void main(String[] args) {
        launch(args);
    }
}