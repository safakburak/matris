package matris.ftp;

import java.io.File;

public interface FileTransferListener {

	void fileSendCompleted(int fileId, boolean success);

	void fileReceiveCompleted(int taskId, int fileId, File file);

}
