package org.springdot.forpan.gui;

import org.springdot.forpan.model.ForpanModel;
import org.springdot.forpan.model.ModelSource;

class Env{
    ForpanModel model = new ForpanModel(ModelSource.getInstance());
    MainWindow mainWindow;
}
