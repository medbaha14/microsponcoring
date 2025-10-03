package com.example.microsponsoringbackend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.example.microsponsoringbackend.service.PerformanceMonitoringService;
import com.example.microsponsoringbackend.util.JwtUtil;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private UserDetailsService userDetailsService;

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/topic", "/queue");
		config.setApplicationDestinationPrefixes("/app");
		config.setUserDestinationPrefix("/user");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws-notifications").setAllowedOriginPatterns("*");
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(new ChannelInterceptor() {
			@Override
			public Message<?> preSend(Message<?> message, MessageChannel channel) {
				StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

				if (StompCommand.CONNECT.equals(accessor.getCommand())) {
					logger.debug("=== STOMP CONNECT FRAME RECEIVED ===");
					logger.debug("Headers: " + accessor.getMessageHeaders());

					String authHeader = accessor.getFirstNativeHeader("Authorization");
					logger.debug("Authorization Header: " + authHeader);

					if (authHeader != null && authHeader.startsWith("Bearer ")) {
						String token = authHeader.substring(7);
						logger.debug("Token preview: " + token.substring(0, 20) + "...");
						logger.debug("Token length: " + token.length());

						try {
							String username = jwtUtil.extractUsername(token);
							logger.debug("Extracted username from token: " + username);

							UserDetails userDetails = userDetailsService.loadUserByUsername(username);
							logger.debug("Loaded user details for: " + username + " with authorities: "
									+ userDetails.getAuthorities());

							if (jwtUtil.validateToken(token, userDetails)) {
								UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
										userDetails, null, userDetails.getAuthorities());
								accessor.setUser(authentication);
								logger.debug("JWT validated, user set in accessor");
							} else {
								logger.debug("Invalid JWT token");
							}
						} catch (Exception e) {
							logger.debug("Error validating JWT: " + e.getMessage());
						}
					} else {
						logger.debug("No valid Authorization header found");
					}

					logger.debug("=====================================");
				}
				return message;
			}
		});
	}

}
