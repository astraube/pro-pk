package sim.android.mtkcit.cittools;

public class CITJNI {
	static {
		System.loadLibrary("citjni");
		// System.load("system/lib/libpsjni.so");
	}

	public native int getPSValue();

	public native int getALSValue();


	public native int[] getPSTHD();

	public native int[] PSCali();
	
	public native int PSCali2(int far ,int close ,int valid);

	public native int PSCali3(int far);
	
	public native int GSCali(int x,int y ,int z);
	
	public native int setLeftSpeaker();
	
	public native int setRightSpeaker();
}
