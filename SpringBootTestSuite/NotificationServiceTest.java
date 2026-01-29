@SpringBootTest
public class NotificationServiceTest {
    @MockBean private NotificationRepository notificationRepository;
    @Autowired private NotificationService notificationService;

    private NotificationDto notificationDto;

    @BeforeEach
    void setUp() {
        notificationDto = new NotificationDto("Test", "Test message", List.of(1L));
    }

    @Test
    void testSendNotification_ValidInput_Success() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());
        Notification result = notificationService.sendNotification(notificationDto);
        assertNotNull(result);
    }

    @Test
    void testSendBulkNotification_ValidInput_Success() {
        doNothing().when(notificationRepository).sendBulk(anyList(), anyString());
        assertDoesNotThrow(() -> notificationService.sendBulkNotification(List.of(1L, 2L), "Bulk message"));
    }

    @Test
    void testCreateAnnouncement_ValidInput_Success() {
        AnnouncementDto announcementDto = new AnnouncementDto("Announcement", "Details");
        when(notificationRepository.saveAnnouncement(any(Announcement.class))).thenReturn(new Announcement());
        Announcement result = notificationService.createAnnouncement(announcementDto);
        assertNotNull(result);
    }

    @Test
    void testCheckQuietHours_WithinQuietHours_ThrowsException() {
        LocalTime quietTime = LocalTime.of(2, 0);
        when(notificationRepository.isQuietHour(quietTime)).thenReturn(true);
        assertThrows(QuietHoursException.class, () -> notificationService.checkQuietHours(quietTime));
    }
}