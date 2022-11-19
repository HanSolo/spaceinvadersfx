module eu.hansolo.fx.spaceinvadersfx {

    // Java-FX
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.swing;
    requires javafx.media;

    // 3rd party
    requires transitive eu.hansolo.toolbox;
    requires transitive eu.hansolo.toolboxfx;

    opens eu.hansolo.fx.spaceinvadersfx to eu.hansolo.toolbox, eu.hansolo.toolboxfx;

    exports eu.hansolo.fx.spaceinvadersfx;
}