package handler;

import dao.AuthDao;
import dao.UserDao;
import dto.AuthDto;
import dto.UserDto;
import org.apache.commons.codec.digest.DigestUtils;
import request.ParsedRequest;
import response.ResponseBuilder;
import response.RestApiAppResponse;
import response.StatusCodes;

import java.time.Instant;

// DONE
public class LoginHandler implements BaseHandler {

    @Override
    public ResponseBuilder handleRequest(ParsedRequest request) {
        // todo
        UserDto userDto = GsonTool.GSON.fromJson(request.getBody(), UserDto.class);
        UserDao userDao = UserDao.getInstance();
        String passwordHash = DigestUtils.sha256Hex(userDto.getPassword());
        var optionalUser = userDao.query("userName", userDto.getUserName()).stream()
                .findFirst()
                .orElse(null);
        if(optionalUser == null || !optionalUser.getPassword().equals(passwordHash)){
            return new ResponseBuilder().setStatus(StatusCodes.UNAUTHORIZED);
        }

        AuthDao authDao = AuthDao.getInstance();
        AuthDto authDto = new AuthDto();
        authDto.setExpireTime(Instant.now().toEpochMilli() - 60000);
        String hash = DigestUtils.sha256Hex(userDto.getUserName() + authDto.getExpireTime());
        authDto.setHash(hash);
        authDto.setUserName(userDto.getUserName());

        authDao.put(authDto);

        return new ResponseBuilder().setStatus(StatusCodes.OK)
                .setHeader("Set-Cookie", "auth=" + hash)
                .setBody(new RestApiAppResponse<>(true, null, null));
    }
}
