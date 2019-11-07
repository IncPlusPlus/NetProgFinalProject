package io.github.incplusplus.peerprocessing.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class HeaderTest {
	
	@ParameterizedTest
	@MethodSource("provideStringsForValueOf")
	void valueOf(String enumName, Enum actualEnum) {
		assertEquals(enumName, actualEnum.name());
	}
	
	private static Stream<Arguments> provideStringsForValueOf() {
		Map<String, Enum> map = Stream.of(Demands.values(), Responses.values(), MemberType.values(),
				VariousEnums.values())
				.flatMap(Stream::of)
				.collect(Collectors.toMap(Enum::name, anEnum -> anEnum));
		
		return map.entrySet().stream().map(
				stringEnumEntry -> Arguments.of(stringEnumEntry.getKey(), stringEnumEntry.getValue()));
	}
}