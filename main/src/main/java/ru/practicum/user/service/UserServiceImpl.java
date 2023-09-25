package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.ExistsException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.model.User;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        if (userRepository.existsByName(userDto.getName())) {
            throw new ExistsException(
                    String.format("Пользователь с именем " + userDto.getName() + " уже существует."));
        }
        User user = userRepository.save(userMapper.toUser(userDto));
        return userMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID [" + userId + "] не найден."));
        userRepository.deleteById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findAllUsers(List<Long> ids, Pageable pageable) {
        return ids == null ? userMapper.toUserDtoList(userRepository.findAll(pageable).getContent()) :
                userMapper.toUserDtoList(userRepository.findAllByIdIn(ids, pageable).getContent());
    }
}
