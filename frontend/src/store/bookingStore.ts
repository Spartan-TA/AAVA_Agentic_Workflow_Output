import create from 'zustand';

type Booking = {
  roomNumber: string;
  guestName: string;
  checkIn: string;
  checkOut: string;
  status: string;
};

type BookingState = {
  bookings: Booking[];
  setBookings: (bookings: Booking[]) => void;
};

const useBookingStore = create<BookingState>(set => ({
  bookings: [
    {
      roomNumber: '101',
      guestName: 'Alice',
      checkIn: '2024-06-01',
      checkOut: '2024-06-05',
      status: 'Confirmed',
    },
    {
      roomNumber: '102',
      guestName: 'Bob',
      checkIn: '2024-06-10',
      checkOut: '2024-06-12',
      status: 'Pending',
    },
  ],
  setBookings: bookings => set({ bookings }),
}));

export default useBookingStore;