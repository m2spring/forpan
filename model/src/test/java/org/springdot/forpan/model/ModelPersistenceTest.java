package org.springdot.forpan.model;

import org.junit.Test;
import org.springdot.forpan.config.ForpanConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.springdot.forpan.config.ForpanConfig.DISABLED_RECORD_BACKUP_PROP;
import static org.springdot.forpan.util.TestUtil.getMethodName;
import static org.springdot.forpan.util.TestUtil.showMethod;

public class ModelPersistenceTest extends ModelTestBase{

    @Test
    public void testBackupConfigurable() throws Exception{
        showMethod();

        ForpanModel model = new ForpanModel();
        FwRecord rec = new FwRecord();
        rec.setForwarder("fwdr");
        model.setRecords(List.of(rec));

        class Tst{
            Tst exec(String configVal, int expectedFileCount){
                setForpanHome(getMethodName(1)+"/"+System.nanoTime());
                File dir = setBackupConfig(configVal);

                model.save();
                model.save();

                System.out.println("* "+DISABLED_RECORD_BACKUP_PROP+"="+configVal);
                String[] entries = dir.list();
                Arrays.stream(entries).sorted().forEach(entry -> System.out.println("  "+entry));

                assertEquals(expectedFileCount,entries.length);

                return this;
            }
        }

        new Tst()
            .exec(null,2)
            .exec("false",3)
            .exec("foodee",3)
            .exec("true",2)
            ;
    }

    private File setBackupConfig(String val){
        File pf = ForpanConfig.getPropertiesFile();
        File dir = pf.getParentFile();
        dir.mkdirs();
        if (val != null){
            try{
                Files.writeString(
                    pf.toPath(),
                    DISABLED_RECORD_BACKUP_PROP+"="+val+"\n"
                );
            }catch (IOException e){
                throw new RuntimeException(e);
            }
        }
        return dir;
    }
}
