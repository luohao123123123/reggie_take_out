import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.luohao.reggie.utils.ValidateCodeUtils;
import org.assertj.core.internal.Bytes;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest(classes = test.class)
public class test {
    public static void main(String[] args) {
        UUID uuid = UUID.randomUUID();
        System.out.println(uuid);
    }

    @Test
    public void test1() {
        Byte bytes;
        byte[] bytes1 = new byte[1024];
    }

    @Test
    public void test2() {
        String s = ValidateCodeUtils.generateValidateCode4String(4);
        System.out.println(s);
        Integer integer = ValidateCodeUtils.generateValidateCode(6);
        System.out.println(integer);


    }

    @Test
    public void test3(){
        System.out.println(IdWorker.getId());
        System.out.println(IdWorker.get32UUID());
        System.out.println(IdWorker.getIdStr());
        System.out.println(IdWorker.getMillisecond());
        System.out.println(IdWorker.getTimeId());

    }
}
