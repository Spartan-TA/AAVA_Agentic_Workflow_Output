import React from 'react';
import useBookingStore from '../store/bookingStore';

const BookingDashboard = () => {
  const { bookings } = useBookingStore();

  return (
    <div className="p-6 max-w-4xl mx-auto">
      <h2 className="text-2xl font-bold mb-4">Room Bookings</h2>
      <div className="overflow-x-auto">
        <table className="min-w-full border rounded">
          <thead className="bg-gray-100">
            <tr>
              <th className="p-2 border">Room</th>
              <th className="p-2 border">Guest</th>
              <th className="p-2 border">Check-in</th>
              <th className="p-2 border">Check-out</th>
              <th className="p-2 border">Status</th>
            </tr>
          </thead>
          <tbody>
            {bookings.map((b, i) => (
              <tr key={i} className="hover:bg-gray-50">
                <td className="p-2 border">{b.roomNumber}</td>
                <td className="p-2 border">{b.guestName}</td>
                <td className="p-2 border">{b.checkIn}</td>
                <td className="p-2 border">{b.checkOut}</td>
                <td className="p-2 border">{b.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default BookingDashboard;