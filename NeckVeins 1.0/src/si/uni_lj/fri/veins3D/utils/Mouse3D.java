package si.uni_lj.fri.veins3D.utils;

import si.uni_lj.fri.veins3D.gui.settings.NeckVeinsSettings;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

public class Mouse3D {
	    private Controller controller = null;
	    private NeckVeinsSettings settings;
        private float axisX,axisY,axisZ,rotX,rotY,rotZ;
        private double[] axis=new double[3];
        private double[] rotate=new double[3];
        private boolean connected;
        
        public Mouse3D(NeckVeinsSettings settings){
        	this.settings=settings;
            for (Controller c : ControllerEnvironment.getDefaultEnvironment().getControllers()) {
                if (c.getType() == Controller.Type.STICK) {
                    controller = c;
                    connected=true;
                }
            }
            if (controller == null) {
                System.err.println("No SpaceNavigator was found.");
                connected=false;
            }
        }
        
        public void pollMouse(){
            connected=controller.poll();
            if(connected){
                for (Component c : controller.getComponents()) {
                    if (c.getName().equals("X Axis")) axisX=c.getPollData();
                    if (c.getName().equals("Y Axis")) axisY=c.getPollData();
                    if (c.getName().equals("Z Axis")) axisZ=c.getPollData();
                    if (c.getName().equals("X Rotation")) rotX=c.getPollData();
                    if (c.getName().equals("Y Rotation")) rotY=c.getPollData();
                    if (c.getName().equals("Z Rotation")) rotZ=c.getPollData();
                    if (c.getName().equals("Button 1")&&c.getPollData()==1) settings.mSelected=true;
                    if (c.getName().equals("Button 0")&&c.getPollData()==1) settings.mSelected=false;
                }
                
                if(settings.mTrans)axisX=axisY=axisZ=0;
                if(settings.mRot)rotX=rotY=rotZ=0;
                
                // If strong axis is selected
                if(settings.mStrong){
                    float[] tmp={axisX,axisY,axisZ,rotX,rotY,rotZ};
                    int indx=0;
                    float max=Math.abs(tmp[indx]);
                    for(int i=1;i<6;i++){
                        if(Math.abs(tmp[i])>max){
                            indx=i;
                            max=Math.abs(tmp[i]);
                        }
                    }
                    for(int i=0;i<6;i++){
                        if(i!=indx)tmp[i]=0;
                    }
                    axisX=tmp[0];
                    axisY=tmp[1];
                    axisZ=tmp[2];
                    rotX=tmp[3];
                    rotY=tmp[4];
                    rotZ=tmp[5];
                }
            }
            else{
                axisX=axisY=axisZ=rotX=rotY=rotZ=0;
            }
        }
        
        public boolean connected(){
            return connected;
        }
        
        public double[] getAxis(){
        	axis[0]=axisX/(settings.sensitivity);
        	axis[1]=axisY/(settings.sensitivity);
        	axis[2]=axisZ/(settings.sensitivity);
        	return axis;
        }
        public double[] getRot(){
        	rotate[0]=rotX/(settings.sensitivity*100);
        	rotate[1]=rotY/(settings.sensitivity*100);
        	rotate[2]=rotZ/(settings.sensitivity*100);
        	return rotate;
        }
        
        public float getAxisX() {
			return axisX/(settings.sensitivity);
		}

		public float getAxisY() {
			return axisY/(settings.sensitivity);
		}

		public float getAxisZ() {
			return axisZ/(settings.sensitivity);
		}

		public float getRotX() {
			return rotX/(settings.sensitivity*100);
		}

		public float getRotY() {
			return rotY/(settings.sensitivity*100);
		}

		public float getRotZ() {
			return rotZ/(settings.sensitivity*100);
		}

		public String getName(){
            return controller.getName();
        }
        public String toString(){
            return controller.getName()+": ("+axisX+","+axisY+","+axisZ+")|("+rotX+","+rotY+","+rotZ+")";
        }
}
