package se.ritzau.gimli;

import java.io.IOException;
import java.net.UnknownHostException;

import se.ritzau.gimli.command.MasterVolumeCommand;
import se.ritzau.gimli.command.MaxVolumeCommand;
import se.ritzau.gimli.command.PowerCommand;
import se.ritzau.gimli.command.VolumeCommand;

public class DenonAVR {
    public static final PowerCommand power = new PowerCommand();
    public static final VolumeCommand masterVolume = new MasterVolumeCommand();
    public static final VolumeCommand maxMasterVolume = new MaxVolumeCommand();

    private DenonCore core;
    
    public DenonAVR(AVRUI ui, Connection connection) {
	this.core = new DenonCore(ui, connection);
	
	// XXX stop the thread?
	connection.startThread();
	
	registerCommands();
    }

    public DenonAVR(AVRUI ui, String address) throws UnknownHostException, IOException {
	this(ui, new SocketConnection(address));
    }
    
    private void registerCommands() {
	core.registerCommand(power);
	core.registerCommand(masterVolume);
	core.registerCommand(maxMasterVolume);
    }
    
    public boolean isConnected() {
	return core.isConnected();
    }
    
    public boolean isPoweredOn() throws InterruptedException {
	return core.get(power);
    }
    
    public void turnOn() {
	core.send(power.buildCommand(true));
    }
    
    public void turnOff() {
	core.send(power.buildCommand(false));
    }
    
    public float getMasterVolume() throws InterruptedException {
	return core.get(masterVolume);
    }
    
    public void setMasterVolume(float value) {
	core.send(masterVolume.buildCommand(value));
    }
}
