/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.sharetask.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.pac4j.core.profile.CommonProfile;
import org.pac4j.springframework.security.authentication.ClientAuthenticationToken;
import org.sharetask.entity.Language;
import org.sharetask.entity.Role;
import org.sharetask.entity.UserInformation;
import org.sharetask.repository.UserInformationRepository;
import org.sharetask.utility.RequestUltil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

/**
 * @author Michal Bocek
 * @since 1.0.0
 */
@Slf4j
public class StoreUserInformationAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	@Inject
	private UserInformationRepository userRepository;

	@Override
	public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		if (authentication instanceof ClientAuthenticationToken) {
			log.debug("Token is pac4j token.");

			String language = Language.EN.getCode(); 
			UsernamePasswordAuthenticationToken authentToken;
			final CommonProfile profile = (CommonProfile)((ClientAuthenticationToken)authentication).getUserProfile();
			if (userRepository.findByUsername(profile.getEmail()) == null) {
				log.debug("User with name: {} doesne exist's. Will be created", profile.getEmail());
				final UserInformation userInformation = new UserInformation(profile.getEmail());
				userInformation.setName(profile.getFirstName());
				userInformation.setSurName(profile.getFamilyName());
				userInformation.setLanguage(language);
				final ArrayList<Role> list = new ArrayList<Role>();
				list.add(Role.ROLE_USER);
				userInformation.setRoles(list);
				userRepository.save(userInformation);
				final List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
				authorities.add(new SimpleGrantedAuthority(Role.ROLE_USER.name()));
				authentToken = new UsernamePasswordAuthenticationToken(profile.getEmail(), "", authorities);
			} else {
				final UserInformation user = userRepository.read(profile.getEmail());
				language = user.getLanguage();
				final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
				authentToken = new UsernamePasswordAuthenticationToken(profile.getEmail(), "", authorities);
			}
			// language cookie
			final Cookie locale = new Cookie(RequestUltil.LOCALE, language);
			locale.setMaxAge(-1);
			locale.setPath("/");
			response.addCookie(locale);
			
			SecurityContextHolder.getContext().setAuthentication(authentToken);
		}

		super.onAuthenticationSuccess(request, response, authentication);
	}
	
	
}
