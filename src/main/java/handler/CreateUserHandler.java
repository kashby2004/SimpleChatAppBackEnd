package handler;

import dao.UserDao;
import dto.UserDto;
import org.apache.commons.codec.digest.DigestUtils;
import request.ParsedRequest;
import response.ResponseBuilder;
import response.RestApiAppResponse;
import response.StatusCodes;

public class CreateUserHandler implements BaseHandler {

    @Override
    public ResponseBuilder handleRequest(ParsedRequest request) {
        UserDto userDto = GsonTool.GSON.fromJson(request.getBody(), UserDto.class);
        UserDao userDao = UserDao.getInstance();
        var existingUserQuery = userDao.query("userName", userDto.getUserName());
        if(!existingUserQuery.isEmpty()){
            return new ResponseBuilder().setStatus(StatusCodes.BAD_REQUEST)
                    .setBody(new RestApiAppResponse<>(false, null, "Username already exists"));
        }

        userDto.setPassword(DigestUtils.sha256Hex(userDto.getPassword()));
        userDao.put(userDto);
        return new ResponseBuilder().setStatus(StatusCodes.OK)
                .setBody(new RestApiAppResponse<>(true, null, null));
    }
}
