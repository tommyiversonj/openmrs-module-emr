package org.openmrs.module.emr.account;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Role;
import org.openmrs.api.PasswordException;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.UserService;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.emr.EmrConstants;
import org.openmrs.util.OpenmrsUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(OpenmrsUtil.class)
public class AccountValidatorTest {
	
	private AccountValidator validator;
    private AccountDomainWrapper account;
    private AccountService accountService;
    private UserService userService;
    private ProviderService providerService;
    private PersonService personService;

    private Role fullPrivileges;
    private Role someCapability;
    private Set<Role> someCapabilitySet;

    @Before
	public void setValidator() {

        accountService = Mockito.mock(AccountService.class);
        userService = Mockito.mock(UserService.class);
        providerService = Mockito.mock(ProviderService.class);
        personService = Mockito.mock(PersonService.class);

        validator = new AccountValidator();
        validator.setMessageSourceService(Mockito.mock(MessageSourceService.class));
        validator.setUserService(userService);
        validator.setProviderService(providerService);

        fullPrivileges = new Role(EmrConstants.PRIVILEGE_LEVEL_FULL_ROLE);
        when(accountService.getAllPrivilegeLevels()).thenReturn(Collections.singletonList(fullPrivileges));

        someCapability = new Role(EmrConstants.ROLE_PREFIX_CAPABILITY + "Some Capability");
        someCapabilitySet = new HashSet<Role>();
        someCapabilitySet.add(someCapability);
        when(accountService.getAllCapabilities()).thenReturn(Collections.singletonList(someCapability));

        Person person = new Person();
        person.addName(new PersonName());
        account = new AccountDomainWrapper(person, accountService, userService, providerService, personService);
	}
	
	/**
	 * @see AccountValidator#validate(Object,Errors)
	 * @verifies reject an empty givenname
	 */
	@Test
	public void validate_shouldRejectAnEmptyGivenname() throws Exception {
		Errors errors = new BindException(account, "account");
		validator.validate(account, errors);
		assertTrue(errors.hasFieldErrors("givenName"));
	}
	
	/**
	 * @see AccountValidator#validate(Object,Errors)
	 * @verifies reject an empty familyname
	 */
	@Test
	public void validate_shouldRejectAnEmptyFamilyname() throws Exception {
		account.setGivenName("give name");
		
		Errors errors = new BindException(account, "account");
		validator.validate(account, errors);
		assertTrue(errors.hasFieldErrors("familyName"));
	}

    @Test
    public void validate_shouldRejectAnEmptyGender() throws Exception {
        account.setGivenName("givenName");
        account.setFamilyName("familyName");

        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);
        assertTrue(errors.hasFieldErrors("gender"));
    }

	/**
	 * @see AccountValidator#validate(Object,Errors)
	 * @verifies reject an empty privilegeLevel if user is not null
	 */
	@Test
	public void validate_shouldRejectAnEmptyPrivilegeLevelIfUserIsNotNull() throws Exception {
		account.setGivenName("give name");
		account.setFamilyName("family name");
        account.setGender("M");
		account.setUsername("username");
		
		Errors errors = new BindException(account, "account");
		validator.validate(account, errors);
		assertTrue(errors.hasFieldErrors("privilegeLevel"));
	}

    /**
     * @see AccountValidator#validate(Object,Errors)
     * @verifies reject an empty privilegeLevel if user is not null
     */
    @Test
    public void validate_shouldRejectAnEmptyPrivilegeLevelIfUserIsPersisted() throws Exception {
        account.setGivenName("give name");
        account.setFamilyName("family name");
        account.setGender("M");
        account.setUsername("username");
        account.getUser().setUserId(1);    // mimick persistence

        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);
        assertTrue(errors.hasFieldErrors("privilegeLevel"));
    }

	/**
	 * @see AccountValidator#validate(Object,Errors)
	 * @verifies reject an empty username if user is not null
	 */
	@Test
	public void validate_shouldRejectAnEmptyUsernameIfUserIsNotNull() throws Exception {
		account.setGivenName("give name");
		account.setFamilyName("family name");
        account.setGender("M");
		account.setPrivilegeLevel(fullPrivileges);
		
		Errors errors = new BindException(account, "account");
		validator.validate(account, errors);
		assertTrue(errors.hasFieldErrors("username"));
	}
	
	/**
	 * @see AccountValidator#validate(Object,Errors)
	 * @verifies reject password and confirm password if they dont match
	 */
	@Test
	public void validate_shouldRejectPasswordAndConfirmPasswordIfTheyDontMatch() throws Exception {
		account.setGivenName("give name");
		account.setFamilyName("family name");
        account.setGender("M");
		account.setUsername("username");
		account.setPassword("password");
		account.setConfirmPassword("confirm password");
		
		Errors errors = new BindException(account, "account");
		validator.validate(account, errors);
		assertTrue(errors.hasFieldErrors("password"));
	}
	
	/**
	 * @see AccountValidator#validate(Object,Errors)
	 * @verifies require confirm password if password is provided
	 */
	@Test
	public void shouldCreateErrorWhenConfirmPasswordIsNotProvided() throws Exception {
		account.setGivenName("give name");
		account.setFamilyName("family name");
        account.setGender("M");
		account.setUsername("username");
		account.setPassword("password");
		
		Errors errors = new BindException(account, "account");
		validator.validate(account, errors);
		assertTrue(errors.hasFieldErrors("password"));
		assertTrue(errors.hasFieldErrors("confirmPassword"));
	}

    /**
     * @see AccountValidator#validate(Object,Errors)
     * @verifies require confirm password if password is provided
     */
    @Test
    public void shouldCreateErrorWhenPasswordIsNotProvided() throws Exception {
        account.setGivenName("give name");
        account.setFamilyName("family name");
        account.setGender("M");
        account.setUsername("username");
        account.setConfirmPassword("password");

        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);
        assertTrue(errors.hasFieldErrors("password"));
        assertTrue(errors.hasFieldErrors("confirmPassword"));
    }

	
	/**
	 * @see AccountValidator#validate(Object,Errors)
	 * @verifies pass for a valid account
	 */
	@Test
	public void validate_shouldPassForAValidAccount() throws Exception {
		account.setUsername("username");
		account.setGivenName("give name");
		account.setFamilyName("family name");
        account.setGender("M");
		account.setPassword("Password123");
		account.setConfirmPassword("Password123");
		account.setPrivilegeLevel(fullPrivileges);
        account.setCapabilities(someCapabilitySet);
		
		Errors errors = new BindException(account, "account");
		validator.validate(account, errors);
		assertFalse(errors.hasErrors());
	}

	/**
	 * @see AccountValidator#validate(Object,Errors)
	 * @verifies require passwords for a new a user account
	 */
	@Test
	public void validate_shouldRequirePasswordsForNewAUserAccount() throws Exception {
        account.setUsername("username");
		account.setGivenName("give name");
		account.setFamilyName("family name");
        account.setGender("M");
		account.setUsername("username");
		account.setPrivilegeLevel(fullPrivileges);
		
		Errors errors = new BindException(account, "account");
		validator.validate(account, errors);
		assertTrue(errors.hasFieldErrors("password"));
		assertTrue(errors.hasFieldErrors("confirmPassword"));
	}

    /**
     * @see AccountValidator#validate(Object,Errors)
     * @verifies don't require a password for an existing user account
     */
    @Test
    public void validate_shouldNotRequirePasswordsForExistingUserAccount() throws Exception {
        account.setUsername("username");
        account.setGivenName("give name");
        account.setFamilyName("family name");
        account.setGender("M");
        account.setUsername("username");
        account.setPrivilegeLevel(fullPrivileges);
        account.getUser().setUserId(1);  // mock making this user persistent

        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);
        assertFalse(errors.hasFieldErrors("password"));
        assertFalse(errors.hasFieldErrors("confirmPassword"));
    }

    @Test
    public void shouldVerifyIfPasswordIsBeingValidated() {
        mockStatic(OpenmrsUtil.class);

        createAccountWithUsernameAs("username");

        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);

        PowerMockito.verifyStatic();
        OpenmrsUtil.validatePassword("username", "password", "systemId");
    }

    @Test
    public void shouldCreateAnErrorMessageWhenPasswordIsWrong(){
        mockStatic(OpenmrsUtil.class);
        PowerMockito.doThrow(new PasswordException("Your Password is too short")).when(OpenmrsUtil.class);
        OpenmrsUtil.validatePassword("username", "password", "systemId");

        createAccountWithUsernameAs("username");

        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);

        assertTrue(errors.hasErrors());

        List<FieldError> errorList = errors.getFieldErrors("password");

        assertThat(errorList.size(), is(1));
    }

    @Test
    public void shouldCreateAnErrorMessageWhenUsernameHasOnlyOneCharacter(){
        mockStatic(OpenmrsUtil.class);

        createAccountWithUsernameAs("a");
        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);

        assertTrue(errors.hasErrors());
        List<FieldError> errorList = errors.getFieldErrors("username");
        assertThat(errorList.size(), is(1));

    }
    @Test
    public void shouldCreateAnErrorMessageWhenUsernameHasMoreThanFiftyCharacters(){
        mockStatic(OpenmrsUtil.class);

        createAccountWithUsernameAs("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);

        assertTrue(errors.hasErrors());
        List<FieldError> errorList = errors.getFieldErrors("username");
        assertThat(errorList.size(), is(1));
    }

    @Test
    public void shouldCreateAnErrorMessageWhenUserNameCharactersAreNotValid() {
        mockStatic(OpenmrsUtil.class);

        createAccountWithUsernameAs("usern@me");
        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);

        assertTrue(errors.hasErrors());
        List<FieldError> errorList = errors.getFieldErrors("username");
        assertThat(errorList.size(), is(1));
    }

    @Test
    public void shouldValidateIfUserNameCharactersAreValid() {
        mockStatic(OpenmrsUtil.class);

        createAccountWithUsernameAs("usern.-_1");
        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);

        assertFalse(errors.hasErrors());
    }

    @Test
    public void shouldCreateAnErrorMessageWhenUserAndProviderAreNull() {
        mockStatic(OpenmrsUtil.class);

        account.setFamilyName("family name");
        account.setGivenName("given Name");

        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);

        assertTrue(errors.hasErrors());
        List<FieldError> errorList = errors.getFieldErrors("provider");
        assertThat(errorList.size(), is(1));
    }

    @Test
    public void shouldCreateErrorMessageIfUserWithNoCapabilities() {
        mockStatic(OpenmrsUtil.class);

        createAccountWithUsernameAs("username");
        account.setCapabilities(new HashSet<Role>());
        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);

        assertTrue(errors.hasErrors());
        List<FieldError> errorList = errors.getFieldErrors("capabilities");
        assertThat(errorList.size(), is(1));
    }

    @Test
    public void shouldCreateErrorMessageIfDuplicateUsername() {
        mockStatic(OpenmrsUtil.class);

        createAccountWithUsernameAs("username");
        when(userService.hasDuplicateUsername(account.getUser())).thenReturn(true);
        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);

        assertTrue(errors.hasErrors());
        List<FieldError> errorList = errors.getFieldErrors("username");
        assertThat(errorList.size(), is(1));
    }

    @Test
    public void shouldCreateErrorMessageIfDuplicateProviderIdentifier() {
        mockStatic(OpenmrsUtil.class);

        createAccountWithUsernameAs("username");
        account.setProviderEnabled(true);
        when(providerService.isProviderIdentifierUnique(account.getProvider())).thenReturn(false);
        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);

        assertTrue(errors.hasErrors());
        List<FieldError> errorList = errors.getFieldErrors("providerIdentifier");
        assertThat(errorList.size(), is(1));
    }

    private void createAccountWithUsernameAs(String username) {
        account.setUsername(username);
        account.setPassword("password");
        account.setConfirmPassword("password");
        account.setFamilyName("family name");
        account.setGivenName("Given Name");
        account.setGender("M");
        account.setPrivilegeLevel(fullPrivileges);
        account.getUser().setSystemId("systemId");
        account.setCapabilities(someCapabilitySet);
    }
}
