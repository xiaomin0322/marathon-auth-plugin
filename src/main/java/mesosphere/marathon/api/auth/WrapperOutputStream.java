package mesosphere.marathon.api.auth;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

public class WrapperOutputStream extends ServletOutputStream{
    private ByteArrayOutputStream bos;

    public WrapperOutputStream(ByteArrayOutputStream bos) {
        this.bos = bos;
    }

    @Override
    public void write(int b) throws IOException {
        bos.write(b);
    }

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setWriteListener(WriteListener arg0) {
		// TODO Auto-generated method stub
		
	}
}